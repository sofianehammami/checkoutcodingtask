package com.checkout.codingtest;

import com.checkout.*;
import com.checkout.common.*;
import com.checkout.payments.BillingInformation;
import com.checkout.payments.PaymentType;
import com.checkout.payments.ShippingDetails;
import com.checkout.payments.links.PaymentLinkRequest;
import com.checkout.payments.links.PaymentLinkResponse;
import com.checkout.previous.CheckoutApi;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class CodeTests {

    public static void main(String[] args) {
        System.out.println(payLink());
    }

    public static String payLink() {
        CheckoutApi api = CheckoutSdk
                .builder()
                .previous()
                .staticKeys()
                .secretKey("sk_test_0b9b5db6-f223-49d0-b68f-f6643dd4f808")
                .environment(Environment.SANDBOX) // or Environment.PRODUCTION
                .build();

        PaymentLinkRequest paymentLinksRequest = PaymentLinkRequest.builder()
                .amount(49L)
                .currency(Currency.GBP)
                .reference("ORD-123A")
                .description("Payment for t-shirts order")
                .expiresIn(604800)
                .customer(new CustomerRequest(null, "brucewayne@email.com", "Bruce Wayne", null))
                .shipping(ShippingDetails.builder()
                        .address(Address.builder()
                                .addressLine1("Checkout")
                                .addressLine2("90 Tottenham Court Road")
                                .city("London")
                                .state("London")
                                .zip("W1T 4TJ")
                                .country(CountryCode.GB)
                                .build())
                        .phone(Phone.builder().countryCode("1").number("415 555 2671").build())
                        .build())
                .billing(BillingInformation.builder()
                        .address(Address.builder()
                                .addressLine1("Checkout")
                                .addressLine2("90 Tottenham Court Road")
                                .city("London")
                                .state("London")
                                .zip("W1T 4TJ")
                                .country(CountryCode.GB)
                                .build())
                        .phone(Phone.builder().countryCode("1").number("415 555 2671").build())
                        .build())
                .capture(true)
                .captureOn(Instant.now().plus(30, ChronoUnit.DAYS))
                .products(
                        Arrays.asList(Product.builder().name("Black T-shirts M size").quantity(3L).price(10L).build(),
                                Product.builder().name("White T-shirts L size").quantity(3L).price(5L).build(),
                                Product.builder().name("Apple T-shirts std size").quantity(2L).price(2L).build())
                )
                .locale("en-GB")
                .paymentType(PaymentType.REGULAR)
                .build();

        try {
            PaymentLinkResponse response = api.paymentLinksClient().createPaymentLink(paymentLinksRequest).get();
            System.out.println(response.getLink("redirect").getHref());
            return response.getReference();
        } catch (CheckoutApiException e) {
            // API error
            String requestId = e.getRequestId();
            int statusCode = e.getHttpStatusCode();
            Map<String, Object> errorDetails = e.getErrorDetails();
        } catch (CheckoutArgumentException e) {
            // Bad arguments
        } catch (CheckoutAuthorizationException e) {
            // Invalid authorization
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return null;
    }
}