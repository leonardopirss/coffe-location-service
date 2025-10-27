package com.api.app_location;

import com.api.app_location.dao.CoffeWorkRepository;
import com.api.app_location.entity.CoffeWork;
import com.api.app_location.service.CoffeWorkService;
import com.api.app_location.service.testService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class AppLocationApplicationTests {

    @Autowired
    testService testService;

    @Test
    void soma() {
        Integer soma = testService.soma(1, 2);
        assertEquals(3, soma);

    }

}
