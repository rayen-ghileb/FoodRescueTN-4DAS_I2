package foodrescuetn4das_i2.surprise;

import foodrescuetn4das_i2.surprise.controllers.SurpriseBasketController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SurpriseBasketController.class)
class SurpriseApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void helloReturnsExactPlainText() throws Exception {
        mockMvc.perform(get("/api/surprise-baskets/hello"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_PLAIN))
                .andExpect(content().string("hello im surprise busket"));
    }
}
