package com.romanidze.perpenanto.validators;

import com.romanidze.perpenanto.domain.user.User;
import com.romanidze.perpenanto.forms.user.UserRegistrationForm;
import com.romanidze.perpenanto.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * 15.04.2018
 *
 * @author Andrey Romanov (steampart@gmail.com)
 * @version 1.0
 */
@Component
public class UserRegistrationFormValidator implements Validator {

    private final UserRepository userRepository;

    private String EMAIL_PATTERN =
            "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";

    @Autowired
    public UserRegistrationFormValidator(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public boolean supports(Class<?> clazz) {
        return clazz.getName().equals(UserRegistrationForm.class.getName());
    }

    @Override
    public void validate(Object target, Errors errors) {

        UserRegistrationForm form = (UserRegistrationForm)target;

        Optional<User> existedUser = this.userRepository.findByLogin(form.getLogin());

        if(existedUser.isPresent()){
            errors.reject("bad.login", "Данный логин занят");
        }

        Field[] fields = UserRegistrationForm.class.getDeclaredFields();

        List<String> fieldsNames = Arrays.stream(fields)
                                         .map(Field::getName)
                                         .collect(Collectors.toList());

        List<String> errorTypes = new ArrayList<>();
        List<String> errorDescription = new ArrayList<>();

        StringBuilder sb1 = new StringBuilder();
        StringBuilder sb2 = new StringBuilder();

        int listSize = fieldsNames.size();

        IntStream.range(0, listSize).forEachOrdered(i -> {
            sb1.append("empty.").append(fieldsNames.get(i));
            sb2.append("Empty ").append(fieldsNames.get(i));
            errorTypes.add(sb1.toString());
            errorDescription.add(sb2.toString());
            sb1.setLength(0);
            sb2.setLength(0);
        });

        IntStream.range(0, listSize)
                 .forEachOrdered(i -> ValidationUtils.rejectIfEmptyOrWhitespace(
                         errors, fieldsNames.get(i), errorTypes.get(i), errorDescription.get(i))
                 );

        Pattern pattern = Pattern.compile(this.EMAIL_PATTERN);
        Matcher matcher = pattern.matcher(form.getEmail());

        if(!matcher.matches()){
            errors.reject("bad.email", "Введите правильно адрес электронной почты");
        }

    }
}
