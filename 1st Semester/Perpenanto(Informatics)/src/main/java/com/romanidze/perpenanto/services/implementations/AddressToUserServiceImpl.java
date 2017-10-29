package com.romanidze.perpenanto.services.implementations;

import com.romanidze.perpenanto.dao.implementations.AddressToUserDAOImpl;
import com.romanidze.perpenanto.dao.interfaces.AddressToUserDAOInterface;
import com.romanidze.perpenanto.models.AddressToUser;
import com.romanidze.perpenanto.services.interfaces.AddressToUserServiceInterface;
import com.romanidze.perpenanto.utils.DBConnection;
import com.romanidze.perpenanto.utils.WorkWithCookie;
import com.romanidze.perpenanto.utils.comparators.CompareAttributes;
import com.romanidze.perpenanto.utils.comparators.StreamCompareAttributes;

import javax.servlet.ServletContext;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.IntStream;

public class AddressToUserServiceImpl implements AddressToUserServiceInterface{

    private ServletContext ctx;

    private AddressToUserServiceImpl(){}

    public AddressToUserServiceImpl(ServletContext ctx){

        this.ctx = ctx;

        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

    }

    @Override
    public List<AddressToUser> getAddressToUsersByCookie(HttpServletRequest req, HttpServletResponse resp) {

        DBConnection dbConnection = new DBConnection(this.ctx.getResourceAsStream("/WEB-INF/properties/db.properties"));

        Map<String, String> configMap = new LinkedHashMap<>();
        configMap.putAll(dbConnection.getDBConfig());

        WorkWithCookie cookieWork = new WorkWithCookie();

        List<AddressToUser> sortedList = new ArrayList<>();


        try(Connection conn = DriverManager.getConnection(configMap.get("db_url"), configMap.get("db_username"),
                                                          configMap.get("db_password"))){

            AddressToUserDAOInterface addressDAO = new AddressToUserDAOImpl(conn);
            List<AddressToUser> currentAddresses = addressDAO.findAll();
            Cookie cookie = cookieWork.getCookieWithType(req, resp);

            int size = 3;

            Function<AddressToUser, String> zero = (AddressToUser addr) -> String.valueOf(addr.getId());
            Function<AddressToUser, String> first = (AddressToUser addr) -> String.valueOf(addr.getUserId());
            Function<AddressToUser, String> second = (AddressToUser addr) -> String.valueOf(addr.getPostalCode());

            List<Function<AddressToUser, String>> functions = Arrays.asList(zero, first, second);
            List<String> indexes = Arrays.asList("0", "1", "2");

            Map<String, Function<AddressToUser, String>> functionMap = new HashMap<>();

            IntStream.range(0, size).forEachOrdered(i -> functionMap.put(indexes.get(i), functions.get(i)));

            CompareAttributes<AddressToUser> compareAttr = new StreamCompareAttributes<>();
            sortedList.addAll(compareAttr.sortList(currentAddresses, functionMap, cookie.getValue()));

        }catch (SQLException e) {
            e.printStackTrace();
        }

        return sortedList;

    }
}
