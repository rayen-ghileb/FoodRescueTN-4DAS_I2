package foodrescuetn4das_i2.surprise;

import foodrescuetn4das_i2.surprise.entity.BasketOrder;
import foodrescuetn4das_i2.surprise.entity.BasketDescription;
import foodrescuetn4das_i2.surprise.entity.PaymentSimulation;
import foodrescuetn4das_i2.surprise.entity.PickupCode;
import foodrescuetn4das_i2.surprise.entity.SurpriseBasket;
import foodrescuetn4das_i2.surprise.repository.BasketOrderRepository;
import foodrescuetn4das_i2.surprise.repository.BasketDescriptionRepository;
import foodrescuetn4das_i2.surprise.repository.PaymentSimulationRepository;
import foodrescuetn4das_i2.surprise.repository.PickupCodeRepository;
import foodrescuetn4das_i2.surprise.repository.SurpriseBasketRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("integration")
@Transactional
class SurprisePersistenceIT {

    @Value("${local.server.port}")
    private int port;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private SurpriseBasketRepository basketRepository;

    @Autowired
    private BasketOrderRepository orderRepository;

    @Autowired
    private PickupCodeRepository pickupCodeRepository;

    @Autowired
    private BasketDescriptionRepository descriptionRepository;

    @Autowired
    private PaymentSimulationRepository paymentSimulationRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void startsWithMigratedPostgresqlAndServesHello() throws Exception {
        assertThat(jdbcTemplate.queryForObject("SELECT current_database()", String.class))
                .isNotEqualTo("surprise_basket_db");
        assertThat(jdbcTemplate.queryForList("""
                SELECT table_name FROM information_schema.tables
                WHERE table_schema = 'public' AND table_type = 'BASE TABLE'
                  AND table_name <> 'flyway_schema_history'
                """, String.class))
                .containsExactlyInAnyOrder("surprise_baskets", "basket_orders", "pickup_codes",
                        "basket_descriptions", "payment_simulations");
        assertThat(jdbcTemplate.queryForObject(
                "SELECT count(*) FROM flyway_schema_history WHERE success AND version = '1'", Integer.class))
                .isEqualTo(1);
        assertThat(jdbcTemplate.queryForObject(
                "SELECT count(*) FROM flyway_schema_history WHERE success AND version = '2'", Integer.class))
                .isEqualTo(1);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://127.0.0.1:" + port + "/api/surprise-baskets/hello"))
                .timeout(Duration.ofSeconds(10))
                .GET()
                .build();
        HttpResponse<String> response = HttpClient.newHttpClient()
                .send(request, HttpResponse.BodyHandlers.ofString());

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.headers().firstValue("content-type").orElseThrow()).startsWith("text/plain");
        assertThat(response.body()).isEqualTo("hello im surprise busket");
    }

    @Test
    void persistsBasketOrdersAndOptionalPickupCode() {
        SurpriseBasket basket = createBasket();
        BasketOrder order = createOrder(basket);
        BasketOrder orderWithoutCode = createOrder(basket);
        PickupCode code = createPickupCode(order, "PICKUP-1");
        pickupCodeRepository.saveAndFlush(code);
        entityManager.clear();

        SurpriseBasket savedBasket = basketRepository.findById(basket.getId()).orElseThrow();
        BasketOrder savedOrder = orderRepository.findById(order.getId()).orElseThrow();
        assertThat(savedBasket.getOrders()).hasSize(2);
        assertThat(savedBasket.getOfferId()).isEqualTo(basket.getOfferId());
        assertThat(savedBasket.getPrice()).isEqualByComparingTo("5.50");
        assertThat(savedOrder.getSurpriseBasket().getId()).isEqualTo(basket.getId());
        assertThat(savedOrder.getBuyerUserId()).isEqualTo(order.getBuyerUserId());
        assertThat(savedOrder.getTotalPrice()).isEqualByComparingTo("11.00");
        assertThat(savedOrder.getPickupCode().getId()).isEqualTo(code.getId());
        assertThat(savedOrder.getPickupCode().getExpiresAt()).isEqualTo(code.getExpiresAt());
        assertThat(savedOrder.getPickupCode().getRedeemedAt()).isNull();
        assertThat(orderRepository.findById(orderWithoutCode.getId()).orElseThrow().getPickupCode()).isNull();
    }

    @Test
    void databaseRejectsTwoPickupCodesForOneOrder() {
        BasketOrder order = createOrder(createBasket());
        pickupCodeRepository.saveAndFlush(createPickupCode(order, "FIRST"));

        assertThatThrownBy(() -> pickupCodeRepository.saveAndFlush(createPickupCode(order, "SECOND")))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void persistsDescriptionsAndOptionalPaymentSimulation() {
        SurpriseBasket basket = createBasket();
        BasketDescription bread = new BasketDescription();
        bread.setSurpriseBasket(basket);
        bread.setCategory("BAKERY");
        bread.setDescription("Assorted bread");
        bread.setAllergenInfo("Contains gluten");
        descriptionRepository.saveAndFlush(bread);

        BasketDescription fruit = new BasketDescription();
        fruit.setSurpriseBasket(basket);
        fruit.setCategory("FRUIT");
        fruit.setDescription("Seasonal fruit");
        descriptionRepository.saveAndFlush(fruit);

        BasketOrder order = createOrder(basket);
        BasketOrder orderWithoutPayment = createOrder(basket);
        PaymentSimulation payment = createPaymentSimulation(order);
        paymentSimulationRepository.saveAndFlush(payment);
        entityManager.clear();

        assertThat(basketRepository.findById(basket.getId()).orElseThrow().getDescriptions()).hasSize(2);
        BasketDescription savedDescription = descriptionRepository.findById(bread.getId()).orElseThrow();
        assertThat(savedDescription.getSurpriseBasket().getId()).isEqualTo(basket.getId());
        assertThat(savedDescription.getCategory()).isEqualTo("BAKERY");
        assertThat(savedDescription.getDescription()).isEqualTo("Assorted bread");
        assertThat(savedDescription.getAllergenInfo()).isEqualTo("Contains gluten");
        assertThat(descriptionRepository.findById(fruit.getId()).orElseThrow().getAllergenInfo()).isNull();

        PaymentSimulation savedPayment = orderRepository.findById(order.getId()).orElseThrow()
                .getPaymentSimulation();
        assertThat(savedPayment.getId()).isEqualTo(payment.getId());
        assertThat(savedPayment.getBasketOrder().getId()).isEqualTo(order.getId());
        assertThat(savedPayment.getAmountTND()).isEqualByComparingTo("11.00");
        assertThat(savedPayment.getStatus()).isEqualTo("PENDING");
        assertThat(savedPayment.getProcessedAt()).isNull();
        assertThat(orderRepository.findById(orderWithoutPayment.getId()).orElseThrow()
                .getPaymentSimulation()).isNull();

        LocalDateTime processedAt = LocalDateTime.of(2027, 1, 1, 12, 0);
        savedPayment.setProcessedAt(processedAt);
        savedPayment.setStatus("SIMULATED_SUCCESS");
        paymentSimulationRepository.saveAndFlush(savedPayment);
        entityManager.clear();
        assertThat(paymentSimulationRepository.findById(payment.getId()).orElseThrow().getProcessedAt())
                .isEqualTo(processedAt);
    }

    @Test
    void databaseRejectsTwoPaymentSimulationsForOneOrder() {
        BasketOrder order = createOrder(createBasket());
        paymentSimulationRepository.saveAndFlush(createPaymentSimulation(order));

        assertThatThrownBy(() -> paymentSimulationRepository.saveAndFlush(createPaymentSimulation(order)))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    private PaymentSimulation createPaymentSimulation(BasketOrder order) {
        PaymentSimulation payment = new PaymentSimulation();
        payment.setBasketOrder(order);
        payment.setAmountTND(order.getTotalPrice());
        payment.setStatus("PENDING");
        return payment;
    }

    private SurpriseBasket createBasket() {
        SurpriseBasket basket = new SurpriseBasket();
        basket.setOfferId(UUID.randomUUID());
        basket.setName("Bakery surprise");
        basket.setPrice(new BigDecimal("5.50"));
        basket.setStatus("AVAILABLE");
        return basketRepository.saveAndFlush(basket);
    }

    private BasketOrder createOrder(SurpriseBasket basket) {
        BasketOrder order = new BasketOrder();
        order.setSurpriseBasket(basket);
        order.setBuyerUserId(UUID.randomUUID());
        order.setQuantity(2);
        order.setTotalPrice(new BigDecimal("11.00"));
        order.setStatus("RESERVED");
        return orderRepository.saveAndFlush(order);
    }

    private PickupCode createPickupCode(BasketOrder order, String value) {
        PickupCode code = new PickupCode();
        code.setBasketOrder(order);
        code.setCode(value);
        code.setExpiresAt(LocalDateTime.of(2027, 1, 1, 18, 0));
        return code;
    }
}
