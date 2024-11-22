package com.amazon.ata.mocking;

import com.amazon.stock.InsufficientStockException;
import com.amazon.stock.Stock;
import com.amazon.stock.StockExchange;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;

class PortfolioManagerTest {
    private Stock amznStock = new Stock("amzn", "Amazon");
    private final BigDecimal currentAmazonStockPrice = BigDecimal.valueOf(1_000L);
    private int quantityInPortfolio = 3;
    private Map<Stock, Integer> portfolioStocks;

    private Stock nonExistentStock = new Stock("id", "name");

    @Mock
    private Portfolio portfolio;

    @Mock
    private StockExchangeClient client;

    @InjectMocks
    private PortfolioManager portfolioManager;

    @BeforeEach
    void setUp() {
        portfolioStocks = new HashMap<>();
        portfolioStocks.put(amznStock, quantityInPortfolio);
        try {
            openMocks(this);
        } catch (Exception e) {
            System.out.println("Portfolio Manager Mocking Failed" + e);
        }
    }

    @Test
    void getMarketValue_portfolioWithStocks_returnsValueOfStocks() {
        // GIVEN
        BigDecimal expectedValue = currentAmazonStockPrice.multiply(BigDecimal.valueOf(quantityInPortfolio));

        // WHEN
        when(portfolio.getStocks()).thenReturn(portfolioStocks);
        when(client.getPrice(amznStock)).thenReturn(currentAmazonStockPrice);
        BigDecimal value = portfolioManager.getMarketValue();

        // THEN
        assertEquals(expectedValue, value);
    }

    @Test
    void buy_existingStock_returnsCostOfBuyingStock() {
        // GIVEN
        int quantityToBuy = 5;
        BigDecimal expectedCost = currentAmazonStockPrice.multiply(BigDecimal.valueOf(quantityToBuy));

        // WHEN
        when(client.submitBuy(amznStock, quantityToBuy)).thenReturn(expectedCost);
        BigDecimal cost = portfolioManager.buy(amznStock, quantityToBuy);

        // THEN
        assertEquals(expectedCost, cost);
    }

    @Test
    void buy_nonExistingStock_returnsNull() {
        // GIVEN
        int quantityToBuy = 5;

        // WHEN
        when(client.submitBuy(nonExistentStock, quantityToBuy)).thenReturn(null);
        BigDecimal cost = portfolioManager.buy(nonExistentStock, quantityToBuy);

        // THEN
        assertNull(cost);
    }

    @Test
    void sell_enoughStocksToSell_returnValueSoldFor() {
        // GIVEN
        int quantityToSell = quantityInPortfolio - 1;
        BigDecimal expectedValue = currentAmazonStockPrice.multiply(BigDecimal.valueOf(quantityToSell));

        // WHEN
        when(client.submitSell(amznStock, quantityToSell)).thenReturn(expectedValue);
        BigDecimal value = portfolioManager.sell(amznStock, quantityToSell);

        // THEN
        assertEquals(expectedValue, value);
    }

    @Test
    void sell_notEnoughStocksToSell_returnZeroValue() throws InsufficientStockException {
        // GIVEN
        int quantityToSell = quantityInPortfolio + 1;
        doThrow(InsufficientStockException.class).when(portfolio).removeStocks(amznStock, quantityToSell);

        // WHEN
        // other option without exception
//        when(client.submitSell(amznStock, quantityToSell)).thenReturn(BigDecimal.ZERO);
        BigDecimal value = portfolioManager.sell(amznStock, quantityToSell);

        // THEN
        assertEquals(BigDecimal.ZERO, value);
    }
}
