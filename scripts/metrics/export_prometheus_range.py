#!/usr/bin/env python3
import argparse
import csv
import json
import sys
import time
import urllib.parse
import urllib.request


DEFAULT_QUERIES = {
    "pod_cpu_seconds_rate": (
        'sum by (namespace,pod) '
        '(rate(container_cpu_usage_seconds_total{namespace="app-location",pod!=""}[2m]))'
    ),
    "pod_memory_working_set_bytes": (
        'sum by (namespace,pod) '
        '(container_memory_working_set_bytes{namespace="app-location",pod!=""})'
    ),
    "target_up": (
        'up{app="app-location-service"}'
    ),
    "app_http_requests_rate": (
        'sum by (app,uri,method,status) '
        '(rate(http_server_requests_seconds_count{app="app-location-service"}[2m]))'
    ),
    "app_http_latency_p95_seconds": (
        'histogram_quantile(0.95, '
        'sum by (le,app,uri,method) '
        '(rate(http_server_requests_seconds_bucket{app="app-location-service"}[2m])))'
    ),
    "app_jvm_memory_used_bytes": (
        'sum by (app,area,id) '
        '(jvm_memory_used_bytes{app="app-location-service"})'
    ),
}


def parse_duration(value):
    units = {"s": 1, "m": 60, "h": 3600}
    try:
        return int(value)
    except ValueError:
        pass
    unit = value[-1]
    if unit not in units:
        raise argparse.ArgumentTypeError("Use seconds or a duration like 30m, 2h.")
    return int(value[:-1]) * units[unit]


def prometheus_get(base_url, path, params):
    url = base_url.rstrip("/") + path + "?" + urllib.parse.urlencode(params)
    with urllib.request.urlopen(url, timeout=20) as response:
        payload = json.loads(response.read().decode("utf-8"))
    if payload.get("status") != "success":
        raise RuntimeError(json.dumps(payload, indent=2))
    return payload["data"]["result"]


def labels_to_key(labels):
    return ",".join(f"{key}={labels[key]}" for key in sorted(labels))


def write_range_csv(base_url, query_name, query, start, end, step, output):
    result = prometheus_get(
        base_url,
        "/api/v1/query_range",
        {"query": query, "start": start, "end": end, "step": step},
    )
    rows = []
    for series in result:
        labels = series.get("metric", {})
        metric_key = labels_to_key(labels)
        for timestamp, value in series.get("values", []):
            rows.append(
                {
                    "query_name": query_name,
                    "timestamp_unix": int(float(timestamp)),
                    "timestamp_iso": time.strftime(
                        "%Y-%m-%dT%H:%M:%SZ", time.gmtime(float(timestamp))
                    ),
                    "value": value,
                    "series": metric_key,
                    "labels_json": json.dumps(labels, sort_keys=True),
                }
            )

    with open(output, "w", newline="", encoding="utf-8") as file:
        writer = csv.DictWriter(
            file,
            fieldnames=[
                "query_name",
                "timestamp_unix",
                "timestamp_iso",
                "value",
                "series",
                "labels_json",
            ],
        )
        writer.writeheader()
        writer.writerows(rows)

    return len(rows), len(result)


def main():
    parser = argparse.ArgumentParser(
        description="Export Prometheus query_range data to CSV."
    )
    parser.add_argument("--base-url", default="http://127.0.0.1:9090")
    parser.add_argument("--query-name", choices=sorted(DEFAULT_QUERIES), required=True)
    parser.add_argument("--query", help="Override the predefined PromQL query.")
    parser.add_argument("--window", default="30m", type=parse_duration)
    parser.add_argument("--step", default="15s")
    parser.add_argument("--output", required=True)
    args = parser.parse_args()

    end = int(time.time())
    start = end - args.window
    query = args.query or DEFAULT_QUERIES[args.query_name]

    try:
        rows, series = write_range_csv(
            args.base_url, args.query_name, query, start, end, args.step, args.output
        )
    except Exception as exc:
        print(f"failed to export metrics: {exc}", file=sys.stderr)
        return 1

    print(f"wrote {rows} rows from {series} series to {args.output}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
