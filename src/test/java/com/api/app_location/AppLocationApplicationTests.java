package com.api.app_location;

import com.api.app_location.dao.CoffeWorkRepository;
import com.api.app_location.entity.CoffeWork;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class AppLocationApplicationTests {

    CoffeWorkRepository coffeWorkRepository = Mockito.mock(CoffeWorkRepository.class);

    @Test
    void deveSalvarEBuscarCoffe() {

    }

}
