package com.checkout.codingtest;

import com.checkout.*;
import com.checkout.common.*;
import com.checkout.payments.BillingInformation;
import com.checkout.payments.PaymentType;
import com.checkout.payments.ShippingDetails;
import com.checkout.payments.links.PaymentLinkRequest;
import com.checkout.payments.links.PaymentLinkResponse;
import com.checkout.payments.previous.request.PaymentRequest;
import com.checkout.payments.previous.request.source.RequestCardSource;
import com.checkout.payments.previous.request.source.RequestTokenSource;
import com.checkout.payments.previous.response.PaymentResponse;
import com.checkout.previous.CheckoutApi;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@Controller
public class CheckoutController {

	/*@GetMapping("/checkout")
	public String greeting(@RequestParam(name="name", required=false, defaultValue="World") String name, Model model) {
		model.addAttribute("name", name);
		return "greeting";
	}*/
    private final double EUROGBPRATE = 0.86;
    @GetMapping("/checkout")
    public String greeting(Model model) {
        return "checkout";
    }

    @GetMapping("/paymentsuccess")
    public String paymentSuccess(Model model) {
        return "paymentsuccess";
    }

    @PostMapping("/charge-card")
    public String checkout(Model model, @RequestBody MultiValueMap<String, String> formData) {

        System.out.println(formData);
        String cardRToken = formData.get("cko-card-token").get(0);
        String currencyStr = formData.get("currency").get(0);
        Long amount = Long.getLong(formData.get("amount").get(0));
        Currency currency = null;
        CountryCode countryCode = null;
        if (currencyStr.endsWith("EUR")) {
            currency = Currency.EUR;
            countryCode = CountryCode.DE;
        } else if (currencyStr.endsWith("GBP")) {
            currency = Currency.GBP;
            countryCode = CountryCode.GB;
        }
        boolean success = chargeCard(cardRToken, currency, countryCode, amount);
        if (success) {
            return "paymentsuccess";
        } else {
            return "paymentsuccess";
        }
    }

    @GetMapping("/paymentlink")
    public ResponseEntity<String> paymentLink(@RequestParam(name = "currency") String currencyStr, @RequestParam(name = "amount") String amountStr) {
        Long amount = Long.parseLong(amountStr);
        System.out.println(amountStr);
        Currency currency = null;
        CountryCode countryCode = null;
        if (currencyStr.endsWith("EUR")) {
            currency = Currency.EUR;
            countryCode = CountryCode.DE;
        } else if (currencyStr.endsWith("GBP")) {
            currency = Currency.GBP;
            countryCode = CountryCode.GB;
        }
        return ResponseEntity.ok(getPaymentLink(currency, countryCode, amount));
    }

    public static String getPaymentLink(Currency currency, CountryCode countryCode, Long amount) {
        CheckoutApi api = CheckoutSdk
                .builder()
                .previous()
                .staticKeys()
                .secretKey("sk_test_0b9b5db6-f223-49d0-b68f-f6643dd4f808")
                .environment(Environment.SANDBOX) // or Environment.PRODUCTION
                .build();
        PaymentLinkRequest paymentLinksRequest = PaymentLinkRequest.builder()
                .amount(amount)
                .currency(currency)
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
                                .country(countryCode)
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
                                .country(countryCode)
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
            System.out.println(paymentLinksRequest);
            System.out.println(response.getLink("redirect").getHref());
            return response.getLink("redirect").getHref();
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

    private boolean chargeCard(String cardRToken, Currency currency, CountryCode countryCode, Long amount) {
        // API Keys
        final CheckoutApi api = CheckoutSdk.builder()
                .previous()
                .staticKeys()
                .publicKey("pk_test_4296fd52-efba-4a38-b6ce-cf0d93639d8a")  // optional, only required for operations related with tokens
                .secretKey("sk_test_0b9b5db6-f223-49d0-b68f-f6643dd4f808")
                .environment(Environment.SANDBOX)  // required
                .build();
        RequestCardSource source = RequestCardSource.builder()
                .name("name")
                .number("number")
                .expiryMonth(12)
                .expiryYear(2025)
                .cvv("123")
                .stored(false)
                .billingAddress(Address.builder()
                        .addressLine1("Checkout")
                        .addressLine2("90 Tottenham Court Road")
                        .city("London")
                        .state("London")
                        .zip("W1T 4TJ")
                        .country(countryCode)
                        .build())
                .phone(Phone.builder().countryCode("1").number("415 555 2671").build())
                .build();
        PaymentRequest paymentRequest = PaymentRequest.builder()
                .source(RequestTokenSource.builder().token(cardRToken).build())
                .capture(true)
                .reference("Basket summary t-shirts")
                .amount(amount)
                .currency(currency)
                .successUrl("https://localhost:8443/paymentsuccess")
                .build();

        try {
            PaymentResponse response = api.paymentsClient().requestPayment(paymentRequest).get();
            return true;// or "requestPayout"
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
        return false;
    }
}
