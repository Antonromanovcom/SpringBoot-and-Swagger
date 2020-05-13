package online.prostobank.clients;


import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class, webEnvironment =  SpringBootTest.WebEnvironment.RANDOM_PORT)
//@AutoConfigureMockMvc
@TestPropertySource(
        locations = "classpath:application.yml")
public abstract class AbstractSpringBootTest {
}
