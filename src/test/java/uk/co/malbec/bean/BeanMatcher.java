package uk.co.malbec.bean;

import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.SelfDescribing;
import org.hamcrest.TypeSafeMatcher;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public class BeanMatcher<T> extends TypeSafeMatcher<T> {

    private Class<T> clazz;
    private Error error;
    private String methodName;
    private Assertion<T, ?> failedAssertion;

    private List<Assertion<T, ?>> assertions = new ArrayList<>();

    public BeanMatcher(Class<T> clazz) {
        this.clazz = clazz;
    }

    public <R> BeanMatcher<T> with(Function<T, R> accessor, Matcher<R> matcher) {
        assertions.add(new Assertion<>(accessor, matcher));
        return this;
    }

    @Override
    protected boolean matchesSafely(T item) {
        if (item == null) {
            this.error = Error.NULL;
            return false;
        }

        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(clazz);
        enhancer.setCallback((MethodInterceptor) (obj, method, args, proxy) -> {
            methodName = method.getName();
            return method.invoke(item, args);
        });

        T proxy = (T) enhancer.create();

        Optional<Assertion<T, ?>> result = assertions.stream()
                .filter(assertion -> {
                    boolean passed = !assertion.getMatcher().matches(assertion.getAccessor().apply((T) proxy));
                    if (!passed) {
                        assertion.setFailedMethod(this.methodName);
                    }
                    return passed;
                })
                .findFirst();

        result.ifPresent(assertion -> {
            this.failedAssertion = assertion;
            this.error = Error.INVALID_ASSERTION;
        });

        return !result.isPresent();
    }


    @Override
    public void describeTo(Description description) {
        if (error == null) {
            description.appendText(" a bean of type ").appendText(clazz.getSimpleName()).appendText(" ...");
        } else {
            description.appendText(" a bean of type ").appendText(clazz.getSimpleName()).appendText(" -> ");
        }

        Description descriptionWrapper = null;
        if (Proxy.isProxyClass(description.getClass())) {
            descriptionWrapper = description;
        } else {
            descriptionWrapper = (Description) Proxy.newProxyInstance(Description.class.getClassLoader(),
                    new Class[]{Description.class}, new DescriptionProxyHandler(description));
        }


        if (error == Error.NULL) {
            description.appendText("Bean must not be null.");
            return;
        }


        if (error == Error.INVALID_ASSERTION) {
            descriptionWrapper.appendText(".").appendText(this.methodName).appendText("()").appendDescriptionOf(this.failedAssertion.getMatcher());
            return;
        }


    }


    protected void describeMismatchSafely(T item, Description mismatchDescription) {
        //mismatchDescription.appendText(" was ");

       /* if (error == Error.NULL) {
            mismatchDescription.appendValue(this.failedAssertion.getAccessor().apply(item));
            return;
        }*/

        if (error == Error.INVALID_ASSERTION) {
            this.failedAssertion.getMatcher().describeMismatch(this.failedAssertion.getAccessor().apply(item), mismatchDescription);
        }
    }

    private enum Error {
        NULL,
        INVALID_ASSERTION
    }


    private static class Assertion<T, R> {

        private Function<T, R> accessor;
        private Matcher<R> matcher;
        private String failedMethod;

        Assertion(Function<T, R> accessor, Matcher<R> matcher) {
            this.accessor = accessor;
            this.matcher = matcher;
        }

        Function<T, R> getAccessor() {
            return accessor;
        }

        Matcher<R> getMatcher() {
            return matcher;
        }

        public String getFailedMethod() {
            return failedMethod;
        }

        public void setFailedMethod(String failedMethod) {
            this.failedMethod = failedMethod;
        }
    }

    public static <T> BeanMatcher<T> isBean(Class<T> clazz) {
        return new BeanMatcher<>(clazz);
    }

    public class DescriptionProxyHandler implements InvocationHandler {

        private boolean latch = false;
        private Description description;

        public DescriptionProxyHandler(Description description) {
            this.description = description;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            if (!latch) {
                this.description.appendText("this");
            }
            latch = true;

            if (method.getName().equals("appendDescriptionOf")) {
                if (!(args[0] instanceof BeanMatcher)) {
                    description.appendText(" ");
                }
                SelfDescribing selfDescribing = (SelfDescribing) args[0];
                selfDescribing.describeTo((Description) proxy);
                ;
                return proxy;
            }
            method.invoke(description, args);
            return proxy;
        }
    }

    public static class DescriptionWrapper implements Description {

        private Description description;


        public DescriptionWrapper(Description description) {
            this.description = description;
        }


        @Override
        public Description appendText(String text) {

            description.appendText(text);
            return this;
        }

        @Override
        public Description appendDescriptionOf(SelfDescribing value) {
            if (!(value instanceof BeanMatcher)) {
                description.appendText(" ");
            }
            description.appendDescriptionOf(value);
            return this;
        }

        @Override
        public Description appendValue(Object value) {
            description.appendValue(value);
            return this;
        }

        @Override
        public <T> Description appendValueList(String start, String separator, String end, T... values) {
            description.appendValueList(start, separator, end, values);
            return this;
        }

        @Override
        public <T> Description appendValueList(String start, String separator, String end, Iterable<T> values) {
            description.appendValueList(start, separator, end, values);
            return this;
        }

        @Override
        public Description appendList(String start, String separator, String end, Iterable<? extends SelfDescribing> values) {
            description.appendList(start, separator, end, values);
            return this;
        }
    }
}
