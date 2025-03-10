package ru.netology.selenide;

import com.codeborne.selenide.CollectionCondition;
import com.codeborne.selenide.Condition;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.*;

public class CityFallingListTest {


    @Test
    public void shouldSelectFromCityListTest() {

        open("http://localhost:9999");

        $("[data-test-id='city'] input.input__control").setValue("Пе");
        $$(".popup.popup_visible .menu-item").shouldHave(CollectionCondition.sizeGreaterThan(0), Duration.ofSeconds(3));
        $$(".popup.popup_visible .menu-item").findBy(Condition.text("Петрозаводск")).click();

        String actualCity = $("[data-test-id='city'] input.input__control").getValue();
        Assertions.assertEquals("Петрозаводск", actualCity);
    }

    //    Должен выбирать первый город в выпадающем списке
    @Test
    public void shouldSelectFirstAvailableCityInListTest() {

        open("http://localhost:9999");
        $("[data-test-id='city'] input.input__control").setValue("Пе");
        $$(".popup.popup_visible .menu-item").first().doubleClick();

        String actualCity = $("[data-test-id='city'] input.input__control").getValue();
        Assertions.assertTrue(actualCity.length() > 0);
    }

    //    Должен убирать выпадающий список после того, как сделан выбор
    @Test
    public void shouldRemoveListAfterCitySelectedTest() {

        open("http://localhost:9999");
        $("[data-test-id='city'] input.input__control").setValue("Пе");
        $$(".popup.popup_visible .menu-item").first().click();


        $(".popup.popup_visible").shouldNotBe(visible, Duration.ofSeconds(3));
        Assertions.assertFalse($(".popup.popup_visible").isDisplayed());
    }
}
