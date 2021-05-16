package ru.gadjini.telegram.smart.bot.commons.service;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.introspect.Annotated;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.Assert;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import ru.gadjini.telegram.smart.bot.commons.jackson.mixin.ReplyKeyboardMixin;

import java.io.File;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JsonTest {

    public static void main(String[] args) {
        testFile();
        testReplyKeyboard();
        testGetter();
        testTypeReference();
        testNoCreator();
    }

    private static void testNoCreator() {
        Jackson json = new Jackson(objectMapper());
        String res = json.writeValueAsString(new InputFile(new File("C:\\t.test")));
        Assert.assertEquals("{\"attachName\":\"attach://t.test\",\"mediaName\":\"t.test\",\"newMediaFile\":\"C:\\\\t.test\",\"isNew\":true}",
                res);
    }

    private static void testTypeReference() {
        Jackson json = new Jackson(objectMapper());
        ExtraGetter test = new ExtraGetter();
        test.setName("test");

        Map<String, ExtraGetter> map = new HashMap<>();
        map.put("test", test);
        String s = json.writeValueAsString(map);
        Assert.assertEquals("{\"test\":{\"name\":\"test\"}}", s);

        Map<String, ExtraGetter> m = json.readValue(s, new TypeReference<>() {
        });
        Assert.assertEquals(map, m);
    }

    private static void testGetter() {
        Jackson json = new Jackson(objectMapper());
        ExtraGetter test = new ExtraGetter();
        test.setName("test");
        String res = json.writeValueAsString(test);
        Assert.assertEquals("{\"name\":\"test\"}", res);
        ExtraGetter extraGetter = json.readValue(res, ExtraGetter.class);

        Assert.assertEquals(test, extraGetter);
    }

    private static void testFile() {
        Jackson json = new Jackson(objectMapper());
        File test = new File("C:\\tt.test");
        String expected = test.getAbsolutePath();
        String s = json.writeValueAsString(test);
        test = json.readValue(s, File.class);
        Assert.assertEquals(expected, test.getAbsolutePath());
    }

    private static void testReplyKeyboard() {
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        List<KeyboardRow> rows = new ArrayList<>();
        rows.add(new KeyboardRow() {{
            add(new KeyboardButton() {{
                setText("Test");
            }});
        }});
        replyKeyboardMarkup.setKeyboard(rows);
        Jackson json = new Jackson(objectMapper());
        String res = json.writeValueAsString(replyKeyboardMarkup);
        String expected = "{\"keyboard\":[[{\"text\":\"Test\"}]],\"class\":\"org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup\"}";
        Assert.assertEquals(expected, res);

        ReplyKeyboard replyKeyboard = json.readValue(expected, ReplyKeyboard.class);

        Assert.assertEquals(replyKeyboardMarkup, replyKeyboard);

        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        inlineKeyboardMarkup.setKeyboard(new ArrayList<>());
        List<InlineKeyboardButton> inlineKeyboardButtons = new ArrayList<>();
        inlineKeyboardButtons.add(new InlineKeyboardButton("Test") {{
            setUrl("https://google.com");
        }});
        inlineKeyboardMarkup.getKeyboard().add(inlineKeyboardButtons);
        expected = "{\"inline_keyboard\":[[{\"text\":\"Test\",\"url\":\"https://google.com\"}]],\"class\":\"org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup\"}";
        res = json.writeValueAsString(inlineKeyboardMarkup);
        Assert.assertEquals(expected, res);
        replyKeyboard = json.readValue(expected, ReplyKeyboard.class);

        Assert.assertEquals(inlineKeyboardMarkup, replyKeyboard);
    }

    private static ObjectMapper objectMapper() {
        return new ObjectMapper()
                .registerModules(new JavaTimeModule())
                .setAnnotationIntrospector(new JacksonAnnotationIntrospector() {
                    @Override
                    protected <A extends Annotation> A _findAnnotation(final Annotated annotated,
                                                                       final Class<A> annoClass) {
                        if (!annotated.hasAnnotation(JsonIgnore.class)
                                && !annotated.hasAnnotation(JsonSerialize.class)) {
                            return super._findAnnotation(annotated, annoClass);
                        }
                        return null;
                    }
                })
                .setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.NONE)
                .setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY)
                .setSerializationInclusion(JsonInclude.Include.NON_NULL)
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .addMixIn(ReplyKeyboard.class, ReplyKeyboardMixin.class);
    }
}