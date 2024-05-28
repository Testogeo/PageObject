package ru.netology.web.test;

import com.codeborne.selenide.Configuration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.chrome.ChromeOptions;
import ru.netology.web.data.DataHelper.CardInfo;
import ru.netology.web.page.DashboardPage;
import ru.netology.web.page.LoginPage;

import java.util.HashMap;
import java.util.Map;

import static com.codeborne.selenide.Selenide.open;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static ru.netology.web.data.DataHelper.*;

public class MoneyTransferTest {
  DashboardPage dashboardPage;
  CardInfo firstCardInfo;
  CardInfo secondCardInfo;
  int firstCardBalance;
  int secondCardBalance;

  @BeforeEach
  void setUp() {


    var loginPage = open("http://localhost:9999", LoginPage.class);
    var authInfo = getAuthInfo();
    var verificationPage = loginPage.validLogin(authInfo);
    var verificationCode = getVerificationCode();

    dashboardPage = verificationPage.validVerify(verificationCode);
    firstCardInfo = getFirstCardInfo();
    secondCardInfo = getSecondCardInfo();
    firstCardBalance = dashboardPage.getCardBalance(getMaskedNumber(firstCardInfo.getCardNumber()));
    secondCardBalance = dashboardPage.getCardBalance(getMaskedNumber(secondCardInfo.getCardNumber()));
  }

  @Test
  void shouldTransferFromFirstToSecondCard() {
    var amount = generateValidAmount(firstCardBalance);
    var expectedBalanceFirstCard = firstCardBalance - amount;
    var expectedBalanceSecondCard = secondCardBalance + amount;
    var transferPage = dashboardPage.selectCardToTransfer(secondCardInfo);
    dashboardPage = transferPage.makeValidTransfer(String.valueOf(amount), firstCardInfo);
    var actualBalanceFirstCard = dashboardPage.getCardBalance(getMaskedNumber(firstCardInfo.getCardNumber()));
    var actualBalanceSecondCard = dashboardPage.getCardBalance(getMaskedNumber(secondCardInfo.getCardNumber()));
    assertAll(() -> assertEquals(expectedBalanceFirstCard, actualBalanceFirstCard),
            () -> assertEquals(expectedBalanceSecondCard, actualBalanceSecondCard));
  }

  @Test
  void shouldGetErrorMessageIfTransferAmountMoreBalance() {
    var amount = generateInvalidAmount(secondCardBalance);
    var transferPage = dashboardPage.selectCardToTransfer(firstCardInfo);
    transferPage.makeTransfer(String.valueOf(amount), secondCardInfo);
    transferPage.findErrorMessage("На карте списания недостаточно средств для перевода");
    var actualBalanceFirstCard = dashboardPage.getCardBalance(getMaskedNumber(firstCardInfo.getCardNumber()));
    var actualBalanceSecondCard = dashboardPage.getCardBalance(getMaskedNumber(secondCardInfo.getCardNumber()));
    assertAll(() -> assertEquals(firstCardBalance, actualBalanceFirstCard),
            () -> assertEquals(secondCardBalance, actualBalanceSecondCard));
  }

  @Test
  void shouldGetErrorMessageIfTransferAmountNull() {
    var transferPage = dashboardPage.selectCardToTransfer(secondCardInfo);
    transferPage.makeTransfer("0", firstCardInfo);
    transferPage.findErrorMessage("Сумма перевода не может быть нулевой");
    var actualBalanceFirstCard = dashboardPage.getCardBalance(getMaskedNumber(firstCardInfo.getCardNumber()));
    var actualBalanceSecondCard = dashboardPage.getCardBalance(getMaskedNumber(secondCardInfo.getCardNumber()));
    assertAll(() -> assertEquals(firstCardBalance, actualBalanceFirstCard),
            () -> assertEquals(secondCardBalance, actualBalanceSecondCard));
  }

  @Test
  void shouldGetErrorMessageIfTransferToSameCard() {
    var amount = generateValidAmount(firstCardBalance);
    var transferPage = dashboardPage.selectCardToTransfer(firstCardInfo);
    transferPage.makeTransfer(String.valueOf(amount), firstCardInfo);
    transferPage.findErrorMessage("Карта перевода не может быть той же, что и карта списания");
    var actualBalanceFirstCard = dashboardPage.getCardBalance(getMaskedNumber(firstCardInfo.getCardNumber()));
    var actualBalanceSecondCard = dashboardPage.getCardBalance(getMaskedNumber(secondCardInfo.getCardNumber()));
    assertAll(() -> assertEquals(firstCardBalance, actualBalanceFirstCard),
            () -> assertEquals(secondCardBalance, actualBalanceSecondCard));
  }
}