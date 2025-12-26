package com.api.app_location;

import com.api.app_location.service.SubtractionService;
import org.junit.Assert;
import org.junit.jupiter.api.Test;

public class SubtractionServiceTest {

    @Test
    public void testSubtraction() {
        SubtractionService subtractionService = new SubtractionService();
        Assert.assertEquals(-2, subtractionService.sub(1, 3));
    }
}
