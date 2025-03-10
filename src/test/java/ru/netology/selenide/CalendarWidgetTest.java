package ru.netology.selenide;


import com.codeborne.selenide.SelenideElement;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.Locale;


import static com.codeborne.selenide.Condition.*;
import static com.codeborne.selenide.Selenide.*;

public class CalendarWidgetTest {

    @BeforeEach
    public void setUp() {
        open("http://localhost:9999");
    }

    //    должен успешно отправлять форму, заполненную валидными данными, с датой выбранной через виджет календаря
    @Test
    public void shouldSelectValidDate() {

        $("[data-test-id='city'] input.input__control").setValue("Москва");

        DateInfo targetDateInfo = generateTargetDate(65);

        $("button .icon").click();

//        переключаем месяц пока не дойдет до нужного
        while (!getCurrentlySelectedMonthName().contains(targetDateInfo.getMonthName())) {
            $(".calendar__arrow.calendar__arrow_direction_right[data-step='1']")
                    .shouldNotHave(attribute("data-disabled", "true"))
                    .shouldBe(visible, Duration.ofSeconds(1))
                    .click();
        }

//        Выбираем дату:
        $$(".calendar__day")
                .filterBy(not(cssClass("calendar__day_type_off"))) // Исключаем недоступные дни
                .findBy(text(targetDateInfo.getFormattedDate()))
                .shouldBe(visible, Duration.ofSeconds(5))         // Ждём видимости дня
                .click();

        $("[data-test-id='name'] input.input__control").setValue("Владислав");
        $("[data-test-id='phone'] input.input__control").setValue("+12345678901");
        $("[data-test-id='agreement'] .checkbox__box").click();
        $("button.button").click();

        String controlPhrase = "Успешно";
        $("[data-test-id='notification'] .notification__content")
                .shouldBe(visible, Duration.ofSeconds(15))
                .shouldHave(text(controlPhrase));
    }

    //    виджет календаря должен открываться и быть доступным после клика как по кнопке,
    //    так и по полю ввода
    @Test
    public void shouldBeVisibleAfterInputFieldAndButtonTest() {

        open("http://localhost:9999");
        $("[data-test-id='city'] input.input__control").setValue("Москва");
        $("button .icon").click();
        $(".popup.popup_visible").shouldBe(visible, Duration.ofSeconds(2));

        Assertions.assertTrue($(".popup.popup_visible").isDisplayed());
        $("[data-test-id='city'] input.input__control").click();

        $("input.input__control").click();
        $(".popup.popup_visible").shouldBe(visible, Duration.ofSeconds(2));
        Assertions.assertTrue($(".popup.popup_visible").isDisplayed());
    }

    //    Виджет календаря должен закрываться после клика по любому другому полю
    @Test
    public void shouldClosePopupAfterClickingOutsideTest() {

        $("button .icon").click();
        $(".popup.popup_visible").shouldBe(visible, Duration.ofSeconds(2));

        Assertions.assertTrue($(".popup.popup_visible").isDisplayed());
        $("[data-test-id='city'] input.input__control").click();
        Assertions.assertFalse($(".popup.popup_visible").isDisplayed());
    }

    // даты ближе чем на 3 от текущей, должны быть недоступны для выбора
    @ParameterizedTest
    @CsvSource({"2", "3", "4"})
    public void shouldNotBeVisibleForSelectingTest(int daysInFuture) {

        DateInfo targetDateInfo = generateTargetDate(daysInFuture);
        $("button .icon").click();

        SelenideElement targetDay = $$(".calendar__day")
                .findBy(text(targetDateInfo.getFormattedDate()));

        if (daysInFuture < 3) {
            targetDay.shouldHave(cssClass("calendar__day_type_off"));
        } else {
            targetDay.shouldNotHave(cssClass("calendar__day_type_off"));
        }
    }


    //    метод для генерации данных по дате. На ввод требуется ввести число дней daysInFuture от сегодняшнего.
    //    Возвращается объект DateInfo с двумя параметрами- отформатированная дата(formattedDate)
    //    и название месяца на русском языке в нижнем регистре (monthName)
    private DateInfo generateTargetDate(int daysInFuture) {
        LocalDate targetDare = LocalDate.now().plusDays(daysInFuture);
        String formattedDate = targetDare.format(DateTimeFormatter.ofPattern("d"));
        String monthName = targetDare.getMonth()
                .getDisplayName(TextStyle.FULL_STANDALONE, Locale.forLanguageTag("ru"));
        return new DateInfo(formattedDate, monthName);
    }

    // метод возвращает текстовое значение месяца, выбранного в данный момент в виджете календаря
    private String getCurrentlySelectedMonthName() {
        return $(".calendar__name").getText().trim().toLowerCase();
    }

    //    вспомогательный класс, хранящий состояние для отформатированной даты и названия месяца
    private static class DateInfo {
        private final String formattedDate;
        private final String monthName;

        public DateInfo(String formattedDate, String monthName) {
            this.formattedDate = formattedDate;
            this.monthName = monthName;
        }

        public String getFormattedDate() {
            return formattedDate;
        }

        public String getMonthName() {
            return monthName;
        }
    }
}


