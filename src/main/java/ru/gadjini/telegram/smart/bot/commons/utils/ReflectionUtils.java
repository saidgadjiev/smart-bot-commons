package ru.gadjini.telegram.smart.bot.commons.utils;

import java.util.ArrayList;
import java.util.Collection;

public class ReflectionUtils {

    public static<I, A> Collection<A> findImplements(Collection<I> impls, Class<A> api) {
        Collection<A> result = new ArrayList<>();

        for (Object impl: impls) {
            if (api.isAssignableFrom(impl.getClass())) {
                result.add((A) impl);
            }
        }

        return result;
    }

    public static Class<?> getClass(String className) {
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
