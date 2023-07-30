$(document).ready(function(){


    var payButton = document.getElementById("pay-button");
    var form = document.getElementById("payment-form");
    Frames.init({
      publicKey: 'pk_test_4296fd52-efba-4a38-b6ce-cf0d93639d8a',
      localization: 'DE-DE',
      debug: true
    });
    var logos = generateLogos();
    function generateLogos() {
      var logos = {};
      logos["card-number"] = {
        src: "card",
        alt: "card number logo",
      };
      logos["expiry-date"] = {
        src: "exp-date",
        alt: "expiry date logo",
      };
      logos["cvv"] = {
        src: "cvv",
        alt: "cvv logo",
      };
      return logos;
    }

    var errors = {};
    errors["card-number"] = "Please enter a valid card number";
    errors["expiry-date"] = "Please enter a valid expiry date";
    errors["cvv"] = "Please enter a valid cvv code";

    Frames.addEventHandler(
      Frames.Events.FRAME_VALIDATION_CHANGED,
      onValidationChanged
    );
    function onValidationChanged(event) {
      var e = event.element;

      if (event.isValid || event.isEmpty) {
        if (e === "card-number" && !event.isEmpty) {
          showPaymentMethodIcon();
        }
        setDefaultIcon(e);
        clearErrorIcon(e);
        clearErrorMessage(e);
      } else {
        if (e === "card-number") {
          clearPaymentMethodIcon();
        }
        setDefaultErrorIcon(e);
        setErrorIcon(e);
        setErrorMessage(e);
      }
    }

    function clearErrorMessage(el) {
      var selector = ".error-message__" + el;
      var message = document.querySelector(selector);
      message.textContent = "";
    }

    function clearErrorIcon(el) {
      var logo = document.getElementById("icon-" + el + "-error");
      logo.style.removeProperty("display");
    }

    function showPaymentMethodIcon(parent, pm) {
      if (parent) parent.classList.add("show");

      var logo = document.getElementById("logo-payment-method");
      if (pm) {
        var name = pm.toLowerCase();
        logo.setAttribute("src", "/img/card-icons/" + name + ".svg");
        logo.setAttribute("alt", pm || "payment method");
      }
      logo.style.removeProperty("display");
    }

    function clearPaymentMethodIcon(parent) {
      if (parent) parent.classList.remove("show");

      var logo = document.getElementById("logo-payment-method");
      logo.style.setProperty("display", "none");
    }

    function setErrorMessage(el) {
      var selector = ".error-message__" + el;
      var message = document.querySelector(selector);
      message.textContent = errors[el];
    }

    function setDefaultIcon(el) {
      var selector = "icon-" + el;
      var logo = document.getElementById(selector);
      logo.setAttribute("th:src", "/img/card-icons/" + logos[el].src + ".svg");
      logo.setAttribute("alt", logos[el].alt);
    }

    function setDefaultErrorIcon(el) {
      var selector = "icon-" + el;
      var logo = document.getElementById(selector);
      logo.setAttribute("th:src", "/img/card-icons/" + logos[el].src + "-error.svg");
      logo.setAttribute("alt", logos[el].alt);
    }

    function setErrorIcon(el) {
      var logo = document.getElementById("icon-" + el + "-error");
      logo.style.setProperty("display", "block");
    }

    Frames.addEventHandler(
      Frames.Events.CARD_VALIDATION_CHANGED,
      cardValidationChanged
    );
    function cardValidationChanged() {
      payButton.disabled = !Frames.isCardValid();
    }

    Frames.addEventHandler(
      Frames.Events.CARD_TOKENIZATION_FAILED,
      onCardTokenizationFailed
    );
    function onCardTokenizationFailed(error) {
      console.log("CARD_TOKENIZATION_FAILED: %o", error);
      Frames.enableSubmitForm();
    }

    Frames.addEventHandler(Frames.Events.CARD_TOKENIZED, onCardTokenized);
    function onCardTokenized(event) {
        console.log(event.token);
        Frames.addCardToken(form, event.token);

        var hiddenCurrency = document.createElement('input');
        hiddenCurrency.type = 'hidden';
        hiddenCurrency.name = "currency";
        hiddenCurrency.value = document.getElementById("select-country").value;
        form.appendChild(hiddenCurrency);
        var hiddenAmount = document.createElement('input');
        hiddenAmount.type = 'hidden';
        hiddenAmount.name = "amount";
        hiddenAmount.value = document.getElementById("inputTotalAmount").text;
        form.appendChild(hiddenAmount);
        form.submit();
        alert("submit");
    }

    Frames.addEventHandler(
      Frames.Events.PAYMENT_METHOD_CHANGED,
      paymentMethodChanged
    );
    function paymentMethodChanged(event) {
      var pm = event.paymentMethod;
      let container = document.querySelector(".icon-container.payment-method");

      if (!pm) {
        clearPaymentMethodIcon(container);
      } else {
        clearErrorIcon("card-number");
        showPaymentMethodIcon(container, pm);
      }
    }

    form.addEventListener("submit", onSubmit);
    function onSubmit(event) {
      event.preventDefault();
      Frames.submitCard();
    }

    const changeSelected = (e) => {
        const $select = document.querySelector('#select-country');
        var currency = document.getElementById("select-country").value;
        var amount = document.getElementById("inputTotalAmount").value;
        var currencyCode = "";
        if(currency.endsWith("GBP")) {
            // unable to call Checkout.com rates API. I always get 401 HTTP error
            amount = amount * 0.86;
            currencyCode = "GBP";
        } else {
            amount = 49;
            currencyCode = "EUR";
        }
        document.getElementById("inputTotalAmount").value = amount;
        console.log(currency);
        console.log(amount);
        $.ajax({
                url: "https://localhost:8443/paymentlink?amount="+amount*100+"&currency="+currency,
                contentType: "application/json",
                type: 'GET',
                success: function(result){
                    console.log(result);
                    document.getElementById("paylink").href = result;
                    document.getElementById("paylink").text = "Pay "+ amount + " "+ currencyCode;
                    document.getElementById("paychoice").style.display="block";
                },
                fail: function() {
                     alert( "error" );
                   }
            })
    };
    document.getElementById("select-country").addEventListener("change", changeSelected);
 });