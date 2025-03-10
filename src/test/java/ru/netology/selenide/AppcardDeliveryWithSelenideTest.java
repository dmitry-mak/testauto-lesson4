package ru.netology.selenide;

import com.codeborne.selenide.Condition;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;

import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.open;


class AppcardDeliveryWithSelenideTest {

    @BeforeEach
    public void setUp() {
        open("http://localhost:9999");
    }

    @Test
    public void shouldSendFormTest() {

//        заполняем форму валидными данными
        $("[data-test-id='city'] input.input__control").setValue("Москва");
        $("[data-test-id='date'] input.input__control").doubleClick().sendKeys(Keys.BACK_SPACE);
        $("[data-test-id='date'] input.input__control").setValue(dateFormatter(3));
        $("[data-test-id='name'] input.input__control").setValue("Иван Петров-Сидоров");
        $("[data-test-id='phone'] input.input__control").setValue("+12345678901");
        $("[data-test-id='agreement'] .checkbox__box").click();
        $("button.button").click();

//        Ожидаем 15 секунд и проверяем видимость уведомления об успешной операции
        $("[data-test-id='notification']").shouldBe(Condition.visible, Duration.ofSeconds(15));
        Assertions.assertTrue($("[data-test-id='notification']").isDisplayed());

    }

    //    Для городов, не являющихся административным центром должно появляться сообщение
    //    "Доставка в выбранный город недоступна"
    @Test
    public void shouldShowMessageForInvalidCityTest() {

        $("[data-test-id='city'] input.input__control").setValue("Балашиха");
        $("[data-test-id='date'] input.input__control").doubleClick().sendKeys(Keys.BACK_SPACE);
        $("[data-test-id='date'] input.input__control").setValue(dateFormatter(3));
        $("[data-test-id='name'] input.input__control").setValue("Владислав");
        $("[data-test-id='phone'] input.input__control").setValue("+12345678901");
        $("[data-test-id='agreement'] .checkbox__box").click();
        $("button.button").click();

        String controlPhrase = "Доставка в выбранный город недоступна";
        WebElement notification = $("[data-test-id='city'].input_invalid .input__sub");
        String actualPhrase = notification.getText();

        Assertions.assertTrue(notification.isDisplayed());
        Assertions.assertEquals(controlPhrase, actualPhrase);
    }

    //    Для названия города, написанного не на кириллице, должно появляться сообщение
    //    "Доставка в выбранный город недоступна"
    @Test
    public void shouldShowMessageForLatinCityTest() {

        $("[data-test-id='city'] input.input__control").setValue("Spb");
        $("[data-test-id='date'] input.input__control").doubleClick().sendKeys(Keys.BACK_SPACE);
        $("[data-test-id='date'] input.input__control").setValue(dateFormatter(3));
        $("[data-test-id='name'] input.input__control").setValue("Владислав");
        $("[data-test-id='phone'] input.input__control").setValue("+12345678901");
        $("[data-test-id='agreement'] .checkbox__box").click();
        $("button.button").click();

        String controlPhrase = "Доставка в выбранный город недоступна";
        WebElement notification = $("[data-test-id='city'].input_invalid .input__sub");
        String actualPhrase = notification.getText();

        Assertions.assertTrue(notification.isDisplayed());
        Assertions.assertEquals(controlPhrase, actualPhrase);
    }

    //    При попытке отправить форму с пустым полем "Город", должно появляться предупреждение
    //    "Поле обязательно для заполнения"
    @Test
    public void shouldShowMessageForEmptyCityTest() {

        $("[data-test-id='date'] input.input__control").doubleClick().sendKeys(Keys.BACK_SPACE);
        $("[data-test-id='date'] input.input__control").setValue(dateFormatter(3));
        $("[data-test-id='name'] input.input__control").setValue("Владислав");
        $("[data-test-id='phone'] input.input__control").setValue("+12345678901");
        $("[data-test-id='agreement'] .checkbox__box").click();
        $("button.button").click();

        String controlPhrase = "Поле обязательно для заполнения";
        WebElement notification = $("[data-test-id='city'].input_invalid .input__sub");
        String actualPhrase = notification.getText();

        Assertions.assertTrue(notification.isDisplayed());
        Assertions.assertEquals(controlPhrase, actualPhrase);
    }

    //    Форма должна успешно отправляться, если введена дата от 3 дней и позже от текущей.
    //    И возвращать ошибку, если выбрана дата на 2 дня (и меньше) позже от текущей
    @ParameterizedTest
    @CsvSource({"2", "3", "4"})
    public void shouldReserveForPlusThreeDaysTest(int daysInFuture) {

        $("[data-test-id='city'] input.input__control").setValue("Москва");
        $("[data-test-id='date'] input.input__control").doubleClick().sendKeys(Keys.BACK_SPACE);
        $("[data-test-id='date'] input.input__control").setValue(dateFormatter(daysInFuture));
        $("[data-test-id='name'] input.input__control").setValue("Владислав");
        $("[data-test-id='phone'] input.input__control").setValue("+12345678901");
        $("[data-test-id='agreement'] .checkbox__box").click();
        $("button.button").click();

        if (daysInFuture == 2) {
            String controlPhraseNegative = "Заказ на выбранную дату невозможен";

            WebElement errorNotification = $("[data-test-id='date'] .input__sub");
            String actualPhraseNegative = errorNotification.getText();
            Assertions.assertTrue(errorNotification.isDisplayed());
            Assertions.assertTrue(actualPhraseNegative.contains(controlPhraseNegative));
        } else {
            String controlPhrasePositive = "Успешно";

            $("[data-test-id='notification']").shouldBe(Condition.visible, Duration.ofSeconds(15));
            Assertions.assertTrue($("[data-test-id='notification']").isDisplayed());
            WebElement successNotification = $("[data-test-id='notification'] .notification__title");
            String actualPhrasePositive = successNotification.getText();
            Assertions.assertTrue(actualPhrasePositive.contains(controlPhrasePositive));
        }
    }

    //   При успешной отправке форму, дата в тексте уведомления должна совпадать с датой,
    //   введенной пользователем
    @Test
    public void shouldReserveForCorrectDateTest() {

        $("[data-test-id='city'] input.input__control").setValue("Москва");
        $("[data-test-id='date'] input.input__control").doubleClick().sendKeys(Keys.BACK_SPACE);
        $("[data-test-id='date'] input.input__control").setValue(dateFormatter(365));
        $("[data-test-id='name'] input.input__control").setValue("Владислав");
        $("[data-test-id='phone'] input.input__control").setValue("+12345678901");
        $("[data-test-id='agreement'] .checkbox__box").click();
        String dateInput = $("[data-test-id='date'] input.input__control").getValue();
        $("button.button").click();

        $("[data-test-id='notification']").shouldBe(Condition.visible, Duration.ofSeconds(15));

        WebElement notificationBodyText = $("[data-test-id='notification'] .notification__content");
        String notificationBodyActualText = notificationBodyText.getText();

        Assertions.assertTrue(notificationBodyActualText.contains(dateInput));
    }

    //    При попытке отправить форму с пустым полем "Дата встречи", должно появляться сообщение "Неверно введена дата"
    @Test
    public void shouldSendErrorNotificationForEmptyDateTest() {

        $("[data-test-id='city'] input.input__control").setValue("Москва");
        $("[data-test-id='date'] input.input__control").doubleClick().sendKeys(Keys.BACK_SPACE);
//        $("[data-test-id='date'] input.input__control").setValue(dateFormatter(365));
        $("[data-test-id='name'] input.input__control").setValue("Владислав");
        $("[data-test-id='phone'] input.input__control").setValue("+12345678901");
        $("[data-test-id='agreement'] .checkbox__box").click();
        $("button.button").click();

        String controlPhrase = "Неверно введена дата";
        WebElement notification = $("[data-test-id='date'] .input_invalid .input__sub");
        String actualPhrase = notification.getText();

        Assertions.assertTrue(actualPhrase.contains(controlPhrase));
    }


    //        Должен принимать букву "Ё" как валидный символ для поля "Имя"
    @Test
    public void shouldAcceptSpecialCharacterInNameTest() {

        $("[data-test-id='city'] input.input__control").setValue("Москва");
        $("[data-test-id='date'] input.input__control").doubleClick().sendKeys(Keys.BACK_SPACE);
        $("[data-test-id='date'] input.input__control").setValue(dateFormatter(3));
        $("[data-test-id='name'] input.input__control").setValue("Пётр");
        $("[data-test-id='phone'] input.input__control").setValue("+12345678901");
        $("[data-test-id='agreement'] .checkbox__box").click();
        $("button.button").click();

        $("[data-test-id='notification']").shouldBe(Condition.visible, Duration.ofSeconds(15));
        Assertions.assertTrue($("[data-test-id='notification']").isDisplayed());
    }

    //    При попытке отправить форму с пустым полем "Фамилия и имя", должно появляться сообщение "Поле обязательно для заполнения"
    @Test
    public void shouldSendErrorNotificationForEmptyNameTest() {

        $("[data-test-id='city'] input.input__control").setValue("Москва");
        $("[data-test-id='date'] input.input__control").doubleClick().sendKeys(Keys.BACK_SPACE);
        $("[data-test-id='date'] input.input__control").setValue(dateFormatter(4));
        $("[data-test-id='phone'] input.input__control").setValue("+12345678901");
        $("[data-test-id='agreement'] .checkbox__box").click();
        $("button.button").click();

        String controlPhrase = "Поле обязательно для заполнения";
        WebElement notification = $("[data-test-id='name'].input_invalid .input__sub");
        String actualPhrase = notification.getText();

        Assertions.assertTrue(actualPhrase.contains(controlPhrase));
    }

    // Поле "Фамилия и имя" должно принимать буквы только кириллического алфавита
    @Test
    public void shouldSendErrorNotificationForLatinNameTest() {

        $("[data-test-id='city'] input.input__control").setValue("Москва");
        $("[data-test-id='date'] input.input__control").doubleClick().sendKeys(Keys.BACK_SPACE);
        $("[data-test-id='date'] input.input__control").setValue(dateFormatter(4));
        $("[data-test-id='name'] input.input__control").setValue("Vladisluff");
        $("[data-test-id='phone'] input.input__control").setValue("+12345678901");
        $("[data-test-id='agreement'] .checkbox__box").click();
        $("button.button").click();

        String controlPhrase = "Допустимы только русские буквы, пробелы и дефисы";
        WebElement notification = $("[data-test-id='name'].input_invalid .input__sub");
        String actualPhrase = notification.getText();

        Assertions.assertTrue(actualPhrase.contains(controlPhrase));
    }

    // для номеров телефона короче или длиннее 11 символов должно появляться предупреждающее сообщение
    @ParameterizedTest
    @CsvSource({"+1234567890",
            "+12345678901",
            "+123456789012"})
    public void shouldShowMessageForLongPhoneTest(String phoneNumber) {
        open("http://localhost:9999");

        $("[data-test-id='city'] input.input__control").setValue("Москва");
        $("[data-test-id='date'] input.input__control").doubleClick().sendKeys(Keys.BACK_SPACE);
        $("[data-test-id='date'] input.input__control").setValue(dateFormatter(4));
        $("[data-test-id='name'] input.input__control").setValue("Владислав");
        $("[data-test-id='phone'] input.input__control").setValue(phoneNumber);
        $("[data-test-id='agreement'] .checkbox__box").click();
        $("button.button").click();
        if (phoneNumber.length() == 12) {
            $("[data-test-id='notification']").shouldBe(Condition.visible, Duration.ofSeconds(15));
            Assertions.assertTrue($("[data-test-id='notification']").isDisplayed());
        } else {

            Assertions.assertTrue(
                    $("[data-test-id='phone'].input_invalid .input__sub").isDisplayed());

            String controlPhrase = "Телефон указан неверно. Должно быть 11 цифр";
            WebElement notification = $("[data-test-id='phone'].input_invalid .input__sub");
            String actualPhrase = notification.getText();

            Assertions.assertTrue(actualPhrase.contains(controlPhrase));
        }
    }

    @Test
    public void shouldShowMessageForLettersInPhoneTest() {
    }

    //    вспомогательный метод. Возвращает отформатированную дату.
//    Параметр int daysInFuture - количество дней, удаленных от текущего
    public String dateFormatter(int daysInFuture) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        LocalDate localDate = LocalDate.now();
        LocalDate dateInFuture = localDate.plusDays(daysInFuture);
        String formattedDate = dateInFuture.format(formatter);
        return formattedDate;
    }

}