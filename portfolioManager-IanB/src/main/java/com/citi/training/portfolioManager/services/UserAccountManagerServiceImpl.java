package com.citi.training.portfolioManager.services;

import com.citi.training.portfolioManager.entities.AccountActivity;
import com.citi.training.portfolioManager.entities.User;
import com.citi.training.portfolioManager.repo.AccountRepository;
import com.citi.training.portfolioManager.repo.UserRepository;
import com.citi.training.portfolioManager.strategy.DateTimeFormatter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.*;

import static org.apache.http.client.utils.DateUtils.parseDate;

@Service
public class UserAccountManagerServiceImpl implements UserAccountManagerService {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private AccountRepository accountActivityRepo;

    @Override
    public Collection<User> getUsers() {
        return userRepository.findAll();
    }

    @Override
    public Double deposit(Double cash, Integer userId) {
        User user = userRepository.getById(userId);
        AccountActivity accountActivity = user.getTodayAccountActivity();
        accountActivity.deposit(cash);
        return accountActivity.getCashValue();
    }

    @Override
    public Double withdraw(Double cash, Integer userId) {
        User user = userRepository.getById(userId);
        AccountActivity accountActivity = user.getTodayAccountActivity();
        // If there's no enough cash in account, withdraw fails.
        if (cash > accountActivity.getCashValue()) return null;
        accountActivity.withdraw(cash);
        return accountActivity.getCashValue();
    }

    @Override
    public Collection<AccountActivity> getAccountActivity() {
        return accountActivityRepo.findAll();
    }


    @Override
    public List<HashMap<String, Object>> getYearToDateCash() throws ParseException {
        List<HashMap<String, Object>> cashInfo = new ArrayList<>();
        Collection<List> cashCollection = accountActivityRepo.getCashValueByMonth(7, 2021);
        for (List cash : cashCollection) {
            HashMap<String, Object> info = new HashMap<>();
            info.put("name", DateTimeFormatter.formatMonth(cash.get(0).toString()));
            info.put("value", cash.get(1));
            cashInfo.add(info);
        }
        return cashInfo;

    }

    @Override
    public List<Double> getYearToDateNetWorth() {
        return null;
    }

    @Override
    public List<Double> getYearToDateTotalEquity() {
        return null;
    }

    @Override
    public void deleteAccountActivity(Date date) {
        accountActivityRepo.deleteById(date);
    }


    @Override
    public Collection<AccountActivity> getAccountActivityByRange(String range) {
        LocalDate today = LocalDate.now();
        Collection<AccountActivity> accountActivities = null;
        int month = today.getMonthValue();
        switch (range) {
            case "lastWeek":
                LocalDate date = today.with(DayOfWeek.MONDAY).minusDays(7);
                Date lastMonday = java.sql.Date.valueOf(date);
                Date lastFriday = java.sql.Date.valueOf(date.plusDays(4));
                accountActivities = accountActivityRepo.getAccountActivitiesByInterval(lastMonday, lastFriday);
                break;
            case "lastMonth":
                accountActivities = accountActivityRepo.getAccountActivitiesByYearAndMonth(month - 1, 2021);
                break;
            default:
                int startMonth = DateTimeFormatter.getLastQuarterStartMonth();
                accountActivities = new ArrayList<>();
                int timeLength = 3;
                if (range.equals("yearToDate")) {
                    startMonth = 1;
                    timeLength = 11;
                }

                for (int i = 0; i < timeLength; i++) {
                    List<AccountActivity> temp = accountActivityRepo.getAccountActivitiesByYearAndMonth(startMonth + i, 2021);
                    accountActivities.add(temp.get(temp.size() - 1));
                }
                break;
        }
        System.out.println(accountActivities);
        return accountActivities;

    }


    private List<HashMap<String, Object>> getAccountInfoByRange(
            String type, String range) {
        Collection<AccountActivity> accountActivities = getAccountActivityByRange(range);
        List<HashMap<String, Object>> accountInfo = new ArrayList<>();
        for (AccountActivity ac : accountActivities) {
            HashMap<String, Object> info = new HashMap<>();
            info.put("name", DateTimeFormatter.formatMonth(ac.getDate().toString()));
            switch (type) {
                case "cashValue":
                    info.put("value", ac.getCashValue());
                    break;
                case "totalEquity":
                    info.put("value", ac.getTotalEquity());
                    break;
                case "investmentValue":
                    info.put("value", ac.getInvestmentValue());
                    break;
                case "netWorth":
                    info.put("value", ac.getNetWorth());
                    break;
            }
            accountInfo.add(info);

        }
        return accountInfo;


    }

    @Override
    public List<HashMap<String, Object>> getNetWorthByRange(String range) {
        return getAccountInfoByRange("netWorth", range);
    }

    @Override
    public List<HashMap<String, Object>> getCashValueByRange(String range) {
        return getAccountInfoByRange("cashValue", range);
    }

    @Override
    public List<HashMap<String, Object>> getInvestmentValueByRange(String range) {
        return getAccountInfoByRange("investmentValue", range);
    }

    @Override
    public List<HashMap<String, Object>> getTotalEquityByRange(String range) {
        return getAccountInfoByRange("totalEquity", range);
    }

}
