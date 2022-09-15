package ai.txai.common.utils;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * Time: 20/05/2022
 * Author Hay
 */
public class ReflectionUtils {
    public static <T> Class<T> getFirstGeneric(Object obj, Class clazz) {
        Type genericSuperclass = obj.getClass().getGenericSuperclass();
        if (genericSuperclass instanceof ParameterizedType) {
            Type[] actualTypeArguments = ((ParameterizedType) genericSuperclass).getActualTypeArguments();
            for (Type argument : actualTypeArguments) {
                if (argument instanceof Class) {
                    Class classArgument = (Class) argument;
                    if (clazz.isAssignableFrom(classArgument)) {
                        return (Class<T>) classArgument;
                    }
                }
            }
        }
        return null;
    }

    public static boolean hasMethod(Class clazz, String methodName) {
        try {
            Method method = clazz.getDeclaredMethod(methodName);
            if (method != null) {
                return true;
            }
        } catch (NoSuchMethodException e) {
            return false;
        }

        return false;
    }
}
