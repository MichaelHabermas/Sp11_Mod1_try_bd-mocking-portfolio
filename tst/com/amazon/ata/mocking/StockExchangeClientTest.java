package com.amazon.ata.mocking;

import com.amazon.stock.BuyStockRequest;
import com.amazon.stock.NonExistentStockException;
import com.amazon.stock.SellStockRequest;
import com.amazon.stock.SellStockResponse;
import com.amazon.stock.Stock;
import com.amazon.stock.StockExchange;
import com.amazon.stock.StockPriceResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;

class StockExchangeClientTest {
    @Mock
    private StockExchange stockExchange;

    private String existingStockSymbol = "wfm";
    private Stock existingStock = new Stock(existingStockSymbol, "Whole Foods");
    private BigDecimal currentExistingStockPrice = BigDecimal.valueOf(100L);

    private String nonExistentStockSymbol = "nonexistent";
    private Stock nonExistentStock = new Stock(nonExistentStockSymbol, "name");

    @InjectMocks
    private StockExchangeClient client;

    @BeforeEach
    public void setUp() {
        openMocks(this);
    }

    @Test
    void getPrice_existingStock_returnsPrice() throws NonExistentStockException {
        // GIVEN
        StockPriceResponse response = StockPriceResponse.builder()
            .withSymbol(existingStockSymbol)
            .withPrice(currentExistingStockPrice)
            .build();
        when(stockExchange.getMarketPrice(existingStockSymbol)).thenReturn(response);

        // WHEN
        BigDecimal price = client.getPrice(existingStock);

        // THEN
        assertEquals(currentExistingStockPrice, price);
    }

    @Test
    void getPrice_nonExistentStock_returnsNull() throws NonExistentStockException {
        // GIVEN
        doThrow(NonExistentStockException.class).when(stockExchange).getMarketPrice(nonExistentStockSymbol);

        // WHEN
        BigDecimal price = client.getPrice(nonExistentStock);

        // THEN
        assertNull(price);
    }

    @Test
    void submitBuy_nonExistentStock_returnsNull() throws NonExistentStockException {
        // GIVEN
        int quantity = 3;
        when(stockExchange.bid(any(BuyStockRequest.class))).thenThrow(NonExistentStockException.class);

        // WHEN
        BigDecimal price = client.submitBuy(nonExistentStock, quantity);

        // THEN
        assertNull(price);
    }

    @Test
    void submitSell_existingStock_returnsPrice() throws NonExistentStockException {
        // GIVEN
        int quantity = 3;
        SellStockResponse response = SellStockResponse.builder()
            .withSymbol(existingStockSymbol)
            .withQuantity(quantity)
            .withPrice(currentExistingStockPrice.multiply(BigDecimal.valueOf(quantity)))
            .build();
        when(stockExchange.offer(any(SellStockRequest.class))).thenReturn(response);

        // WHEN
        BigDecimal price = client.submitSell(existingStock, quantity);

        // THEN
        assertEquals(currentExistingStockPrice.multiply(BigDecimal.valueOf(quantity)), price);
    }

    @Test
    void submitSell_nonExistentStock_returnsNull() throws NonExistentStockException {
        // GIVEN
        int quantity = 3;
        when(stockExchange.offer(any(SellStockRequest.class))).thenThrow(NonExistentStockException.class);

        // WHEN
        BigDecimal price = client.submitSell(nonExistentStock, quantity);

        // THEN
        assertNull(price);
    }
}
