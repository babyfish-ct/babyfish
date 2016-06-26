/*
 * BabyFish, Object Model Framework for Java and JPA.
 * https://github.com/babyfish-ct/babyfish
 *
 * Copyright (c) 2008-2016, Tao Chen
 *
 * This copyrighted material is made available to anyone wishing to use, modify,
 * copy, or redistribute it subject to the terms and conditions of the GNU
 * Lesser General Public License, as published by the Free Software Foundation.
 *
 * Please visit "http://opensource.org/licenses/LGPL-3.0" to know more.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License
 * for more details.
 */
package org.babyfish.lang;

import java.util.function.Function;

/**
 * @author Tao Chen
 */
public class Strings {
    
    private static final String DEFAULT_SEPERATOR = ", ";

    protected Strings() {
        throw new UnsupportedOperationException();
    }
    
    public static String toCamelCase(String str) {
        if (Nulls.isNullOrEmpty(str) || Character.isLowerCase(str.charAt(0))) {
            return str;
        }
        int index = 0;
        int len = str.length();
        char[] buf = new char[len];
        while (index < len) {
            char c = str.charAt(index);
            if (!Character.isUpperCase(c)) {
                break;
            }
            buf[index++] = Character.toLowerCase(c);
        }
        if (index == 0) {
            return str;
        }
        if (index == len) {
            return new String(buf);
        }
        if (index == 1) {
            return Character.toLowerCase(buf[0]) + str.substring(1);
        }
        return new String(buf, 0, index - 1) + str.substring(index - 1);
    }
    
    public static String toPascalCase(String str) {
        if (Nulls.isNullOrEmpty(str) || Character.isUpperCase(str.charAt(0))) {
            return str;
        }
        return Character.toUpperCase(str.charAt(0)) + str.substring(1);
    }
    
    public static String join(
            Iterable<?> iterable) {
        StringBuilder builder = new StringBuilder();
        join(iterable, DEFAULT_SEPERATOR, null, builder);
        return builder.toString();
    }

    public static String join(
            Iterable<?> iterable,
            String sperator) {
        StringBuilder builder = new StringBuilder();
        join(iterable, sperator, null, builder);
        return builder.toString();
    }
    
    public static <T> String join(
            Iterable<T> iterable,
            Function<T, String> toStringFunc) {
        StringBuilder builder = new StringBuilder();
        join(iterable, DEFAULT_SEPERATOR, toStringFunc, builder);
        return builder.toString();
    }

    public static <T> String join(
            Iterable<T> iterable,
            String seperator,
            Function<T, String> toStringFunc) {
        StringBuilder builder = new StringBuilder();
        join(iterable, seperator, toStringFunc, builder);
        return builder.toString();
    }
    
    public static void join(
            Iterable<?> iterable,
            StringBuilder outputStringBuilder) {
        join(iterable, DEFAULT_SEPERATOR, null, outputStringBuilder);
    }

    public static void join(
            Iterable<?> iterable,
            String sperator,
            StringBuilder outputStringBuilder) {
        join(iterable, sperator, null, outputStringBuilder);
    }
    
    public static <T> void join(
            Iterable<T> iterable,
            Function<T, String> toStringFunc,
            StringBuilder outputStringBuilder) {
        join(iterable, DEFAULT_SEPERATOR, toStringFunc, outputStringBuilder);
    }

    public static void join(
            Iterable<?> iterable,
            StringBuffer outputStringBuffer) {
        join(iterable, DEFAULT_SEPERATOR, null, outputStringBuffer);
    }

    public static void join(
            Iterable<?> iterable,
            String sperator,
            StringBuffer outputStringBuffer) {
        join(iterable, sperator, null, outputStringBuffer);
    }
    
    public static <T> void join(
            Iterable<T> iterable,
            Function<T, String> toStringFunc,
            StringBuffer outputStringBuffer) {
        join(iterable, DEFAULT_SEPERATOR, toStringFunc, outputStringBuffer);
    }

    public static <T> void join(
            Iterable<T> iterable,
            String seperator,
            Function<T, String> toStringFunc,
            StringBuilder outputStringBuilder) {
        Arguments.mustNotBeNull("iterable", iterable);
        Arguments.mustNotBeNull("outputStringBuilder", outputStringBuilder);
        if (seperator == null || seperator.isEmpty()) {
            if (toStringFunc == null) {
                for (T element : iterable) {
                    outputStringBuilder.append(element);
                }
            } else {
                for (T element : iterable) {
                    outputStringBuilder.append(toStringFunc.apply(element));
                }
            }
        } else {
            boolean addSeperator = false;
            if (toStringFunc == null) {
                for (T element : iterable) {
                    if (addSeperator) {
                        outputStringBuilder.append(seperator);
                    } else {
                        addSeperator = true;
                    }
                    outputStringBuilder.append(element);
                }
            } else {
                for (T element : iterable) {
                    if (addSeperator) {
                        outputStringBuilder.append(seperator);
                    } else {
                        addSeperator = true;
                    }
                    outputStringBuilder.append(toStringFunc.apply(element));
                }
            }
        }
    }

    public static <T> void join(
            Iterable<T> iterable,
            String seperator,
            Function<T, String> toStringFunc,
            StringBuffer outputStringBuffer) {
        Arguments.mustNotBeNull("iterable", iterable);
        Arguments.mustNotBeNull("outputStringBuffer", outputStringBuffer);
        if (seperator == null || seperator.isEmpty()) {
            if (toStringFunc == null) {
                for (T element : iterable) {
                    outputStringBuffer.append(element);
                }
            } else {
                for (T element : iterable) {
                    outputStringBuffer.append(toStringFunc.apply(element));
                }
            }
        } else {
            boolean addSeperator = false;
            if (toStringFunc == null) {
                for (T element : iterable) {
                    if (addSeperator) {
                        outputStringBuffer.append(seperator);
                    } else {
                        addSeperator = true;
                    }
                    outputStringBuffer.append(element);
                }
            } else {
                for (T element : iterable) {
                    if (addSeperator) {
                        outputStringBuffer.append(seperator);
                    } else {
                        addSeperator = true;
                    }
                    outputStringBuffer.append(toStringFunc.apply(element));
                }
            }
        }
    }
    
    public static <T> String join(
            T[] arr) {
        StringBuilder builder = new StringBuilder();
        join(arr, DEFAULT_SEPERATOR, null, builder);
        return builder.toString();
    }

    public static <T> String join(
            T[] arr,
            String sperator) {
        StringBuilder builder = new StringBuilder();
        join(arr, sperator, null, builder);
        return builder.toString();
    }
    
    public static <T> String join(
            T[] arr,
            Function<T, String> toStringFunc) {
        StringBuilder builder = new StringBuilder();
        join(arr, DEFAULT_SEPERATOR, toStringFunc, builder);
        return builder.toString();
    }

    public static <T> String join(
            T[] arr,
            String seperator,
            Function<T, String> toStringFunc) {
        StringBuilder builder = new StringBuilder();
        join(arr, seperator, toStringFunc, builder);
        return builder.toString();
    }
    
    public static <T> void join(
            T[] arr,
            StringBuilder outputStringBuilder) {
        join(arr, DEFAULT_SEPERATOR, null, outputStringBuilder);
    }

    public static <T> void join(
            T[] arr,
            String sperator,
            StringBuilder outputStringBuilder) {
        join(arr, sperator, null, outputStringBuilder);
    }
    
    public static <T> void join(
            T[] arr,
            Function<T, String> toStringFunc,
            StringBuilder outputStringBuilder) {
        join(arr, DEFAULT_SEPERATOR, toStringFunc, outputStringBuilder);
    }

    public static <T> void join(
            T[] arr,
            StringBuffer outputStringBuffer) {
        join(arr, DEFAULT_SEPERATOR, null, outputStringBuffer);
    }

    public static <T> void join(
            T[] arr,
            String sperator,
            StringBuffer outputStringBuffer) {
        join(arr, sperator, null, outputStringBuffer);
    }
    
    public static <T> void join(
            T[] arr,
            Function<T, String> toStringFunc,
            StringBuffer outputStringBuffer) {
        join(arr, DEFAULT_SEPERATOR, toStringFunc, outputStringBuffer);
    }

    public static <T> void join(
            T[] arr,
            String seperator,
            Function<T, String> toStringFunc,
            StringBuilder outputStringBuilder) {
        Arguments.mustNotBeNull("arr", arr);
        Arguments.mustNotBeNull("outputStringBuilder", outputStringBuilder);
        if (seperator == null || seperator.isEmpty()) {
            if (toStringFunc == null) {
                for (T element : arr) {
                    outputStringBuilder.append(element);
                }
            } else {
                for (T element : arr) {
                    outputStringBuilder.append(toStringFunc.apply(element));
                }
            }
        } else {
            boolean addSeperator = false;
            if (toStringFunc == null) {
                for (T element : arr) {
                    if (addSeperator) {
                        outputStringBuilder.append(seperator);
                    } else {
                        addSeperator = true;
                    }
                    outputStringBuilder.append(element);
                }
            } else {
                for (T element : arr) {
                    if (addSeperator) {
                        outputStringBuilder.append(seperator);
                    } else {
                        addSeperator = true;
                    }
                    outputStringBuilder.append(toStringFunc.apply(element));
                }
            }
        }
    }

    public static <T> void join(
            T[] arr,
            String seperator,
            Function<T, String> toStringFunc,
            StringBuffer outputStringBuffer) {
        Arguments.mustNotBeNull("arr", arr);
        Arguments.mustNotBeNull("outputStringBuffer", outputStringBuffer);
        if (seperator == null || seperator.isEmpty()) {
            if (toStringFunc == null) {
                for (T element : arr) {
                    outputStringBuffer.append(element);
                }
            } else {
                for (T element : arr) {
                    outputStringBuffer.append(toStringFunc.apply(element));
                }
            }
        } else {
            boolean addSeperator = false;
            if (toStringFunc == null) {
                for (T element : arr) {
                    if (addSeperator) {
                        outputStringBuffer.append(seperator);
                    } else {
                        addSeperator = true;
                    }
                    outputStringBuffer.append(element);
                }
            } else {
                for (T element : arr) {
                    if (addSeperator) {
                        outputStringBuffer.append(seperator);
                    } else {
                        addSeperator = true;
                    }
                    outputStringBuffer.append(toStringFunc.apply(element));
                }
            }
        }
    }
    
    public static <T> String join(
            boolean[] arr) {
        StringBuilder builder = new StringBuilder();
        join(arr, DEFAULT_SEPERATOR, null, builder);
        return builder.toString();
    }

    public static String join(
            boolean[] arr,
            String sperator) {
        StringBuilder builder = new StringBuilder();
        join(arr, sperator, null, builder);
        return builder.toString();
    }
    
    public static String join(
            boolean[] arr,
            Function<Boolean, String> toStringFunc) {
        StringBuilder builder = new StringBuilder();
        join(arr, DEFAULT_SEPERATOR, toStringFunc, builder);
        return builder.toString();
    }

    public static String join(
            boolean[] arr,
            String seperator,
            Function<Boolean, String> toStringFunc) {
        StringBuilder builder = new StringBuilder();
        join(arr, seperator, toStringFunc, builder);
        return builder.toString();
    }
    
    public static void join(
            boolean[] arr,
            StringBuilder outputStringBuilder) {
        join(arr, DEFAULT_SEPERATOR, null, outputStringBuilder);
    }

    public static void join(
            boolean[] arr,
            String sperator,
            StringBuilder outputStringBuilder) {
        join(arr, sperator, null, outputStringBuilder);
    }
    
    public static void join(
            boolean[] arr,
            Function<Boolean, String> toStringFunc,
            StringBuilder outputStringBuilder) {
        join(arr, DEFAULT_SEPERATOR, toStringFunc, outputStringBuilder);
    }

    public static void join(
            boolean[] arr,
            StringBuffer outputStringBuffer) {
        join(arr, DEFAULT_SEPERATOR, null, outputStringBuffer);
    }

    public static void join(
            boolean[] arr,
            String sperator,
            StringBuffer outputStringBuffer) {
        join(arr, sperator, null, outputStringBuffer);
    }
    
    public static void join(
            boolean[] arr,
            Function<Boolean, String> toStringFunc,
            StringBuffer outputStringBuffer) {
        join(arr, DEFAULT_SEPERATOR, toStringFunc, outputStringBuffer);
    }
    
    public static void join(
            boolean[] arr,
            String seperator,
            Function<Boolean, String> toStringFunc,
            StringBuilder outputStringBuilder) {
        Arguments.mustNotBeNull("arr", arr);
        Arguments.mustNotBeNull("outputStringBuilder", outputStringBuilder);
        if (seperator == null || seperator.isEmpty()) {
            if (toStringFunc == null) {
                for (boolean element : arr) {
                    outputStringBuilder.append(element);
                }
            } else {
                for (boolean element : arr) {
                    outputStringBuilder.append(toStringFunc.apply(element));
                }
            }
        } else {
            boolean addSeperator = false;
            if (toStringFunc == null) {
                for (boolean element : arr) {
                    if (addSeperator) {
                        outputStringBuilder.append(seperator);
                    } else {
                        addSeperator = true;
                    }
                    outputStringBuilder.append(element);
                }
            } else {
                for (boolean element : arr) {
                    if (addSeperator) {
                        outputStringBuilder.append(seperator);
                    } else {
                        addSeperator = true;
                    }
                    outputStringBuilder.append(toStringFunc.apply(element));
                }
            }
        }
    }
    
    public static void join(
            boolean[] arr,
            String seperator,
            Function<Boolean, String> toStringFunc,
            StringBuffer outputStringBuffer) {
        Arguments.mustNotBeNull("arr", arr);
        Arguments.mustNotBeNull("outputStringBuffer", outputStringBuffer);
        if (seperator == null || seperator.isEmpty()) {
            if (toStringFunc == null) {
                for (boolean element : arr) {
                    outputStringBuffer.append(element);
                }
            } else {
                for (boolean element : arr) {
                    outputStringBuffer.append(toStringFunc.apply(element));
                }
            }
        } else {
            boolean addSeperator = false;
            if (toStringFunc == null) {
                for (boolean element : arr) {
                    if (addSeperator) {
                        outputStringBuffer.append(seperator);
                    } else {
                        addSeperator = true;
                    }
                    outputStringBuffer.append(element);
                }
            } else {
                for (boolean element : arr) {
                    if (addSeperator) {
                        outputStringBuffer.append(seperator);
                    } else {
                        addSeperator = true;
                    }
                    outputStringBuffer.append(toStringFunc.apply(element));
                }
            }
        }
    }
    
    public static <T> String join(
            char[] arr) {
        StringBuilder builder = new StringBuilder();
        join(arr, DEFAULT_SEPERATOR, null, builder);
        return builder.toString();
    }

    public static String join(
            char[] arr,
            String sperator) {
        StringBuilder builder = new StringBuilder();
        join(arr, sperator, null, builder);
        return builder.toString();
    }
    
    public static String join(
            char[] arr,
            Function<Character, String> toStringFunc) {
        StringBuilder builder = new StringBuilder();
        join(arr, DEFAULT_SEPERATOR, toStringFunc, builder);
        return builder.toString();
    }

    public static String join(
            char[] arr,
            String seperator,
            Function<Character, String> toStringFunc) {
        StringBuilder builder = new StringBuilder();
        join(arr, seperator, toStringFunc, builder);
        return builder.toString();
    }
    
    public static void join(
            char[] arr,
            StringBuilder outputStringBuilder) {
        join(arr, DEFAULT_SEPERATOR, null, outputStringBuilder);
    }

    public static void join(
            char[] arr,
            String sperator,
            StringBuilder outputStringBuilder) {
        join(arr, sperator, null, outputStringBuilder);
    }
    
    public static void join(
            char[] arr,
            Function<Character, String> toStringFunc,
            StringBuilder outputStringBuilder) {
        join(arr, DEFAULT_SEPERATOR, toStringFunc, outputStringBuilder);
    }

    public static void join(
            char[] arr,
            StringBuffer outputStringBuffer) {
        join(arr, DEFAULT_SEPERATOR, null, outputStringBuffer);
    }

    public static void join(
            char[] arr,
            String sperator,
            StringBuffer outputStringBuffer) {
        join(arr, sperator, null, outputStringBuffer);
    }
    
    public static void join(
            char[] arr,
            Function<Character, String> toStringFunc,
            StringBuffer outputStringBuffer) {
        join(arr, DEFAULT_SEPERATOR, toStringFunc, outputStringBuffer);
    }
    
    public static void join(
            char[] arr,
            String seperator,
            Function<Character, String> toStringFunc,
            StringBuilder outputStringBuilder) {
        Arguments.mustNotBeNull("arr", arr);
        Arguments.mustNotBeNull("outputStringBuilder", outputStringBuilder);
        if (seperator == null || seperator.isEmpty()) {
            if (toStringFunc == null) {
                for (char element : arr) {
                    outputStringBuilder.append(element);
                }
            } else {
                for (char element : arr) {
                    outputStringBuilder.append(toStringFunc.apply(element));
                }
            }
        } else {
            boolean addSeperator = false;
            if (toStringFunc == null) {
                for (char element : arr) {
                    if (addSeperator) {
                        outputStringBuilder.append(seperator);
                    } else {
                        addSeperator = true;
                    }
                    outputStringBuilder.append(element);
                }
            } else {
                for (char element : arr) {
                    if (addSeperator) {
                        outputStringBuilder.append(seperator);
                    } else {
                        addSeperator = true;
                    }
                    outputStringBuilder.append(toStringFunc.apply(element));
                }
            }
        }
    }
    
    public static void join(
            char[] arr,
            String seperator,
            Function<Character, String> toStringFunc,
            StringBuffer outputStringBuffer) {
        Arguments.mustNotBeNull("arr", arr);
        Arguments.mustNotBeNull("outputStringBuffer", outputStringBuffer);
        if (seperator == null || seperator.isEmpty()) {
            if (toStringFunc == null) {
                for (char element : arr) {
                    outputStringBuffer.append(element);
                }
            } else {
                for (char element : arr) {
                    outputStringBuffer.append(toStringFunc.apply(element));
                }
            }
        } else {
            boolean addSeperator = false;
            if (toStringFunc == null) {
                for (char element : arr) {
                    if (addSeperator) {
                        outputStringBuffer.append(seperator);
                    } else {
                        addSeperator = true;
                    }
                    outputStringBuffer.append(element);
                }
            } else {
                for (char element : arr) {
                    if (addSeperator) {
                        outputStringBuffer.append(seperator);
                    } else {
                        addSeperator = true;
                    }
                    outputStringBuffer.append(toStringFunc.apply(element));
                }
            }
        }
    }
    
    public static <T> String join(
            byte[] arr) {
        StringBuilder builder = new StringBuilder();
        join(arr, DEFAULT_SEPERATOR, null, builder);
        return builder.toString();
    }

    public static String join(
            byte[] arr,
            String sperator) {
        StringBuilder builder = new StringBuilder();
        join(arr, sperator, null, builder);
        return builder.toString();
    }
    
    public static String join(
            byte[] arr,
            Function<Byte, String> toStringFunc) {
        StringBuilder builder = new StringBuilder();
        join(arr, DEFAULT_SEPERATOR, toStringFunc, builder);
        return builder.toString();
    }

    public static String join(
            byte[] arr,
            String seperator,
            Function<Byte, String> toStringFunc) {
        StringBuilder builder = new StringBuilder();
        join(arr, seperator, toStringFunc, builder);
        return builder.toString();
    }
    
    public static void join(
            byte[] arr,
            StringBuilder outputStringBuilder) {
        join(arr, DEFAULT_SEPERATOR, null, outputStringBuilder);
    }

    public static void join(
            byte[] arr,
            String sperator,
            StringBuilder outputStringBuilder) {
        join(arr, sperator, null, outputStringBuilder);
    }
    
    public static void join(
            byte[] arr,
            Function<Byte, String> toStringFunc,
            StringBuilder outputStringBuilder) {
        join(arr, DEFAULT_SEPERATOR, toStringFunc, outputStringBuilder);
    }

    public static void join(
            byte[] arr,
            StringBuffer outputStringBuffer) {
        join(arr, DEFAULT_SEPERATOR, null, outputStringBuffer);
    }

    public static void join(
            byte[] arr,
            String sperator,
            StringBuffer outputStringBuffer) {
        join(arr, sperator, null, outputStringBuffer);
    }
    
    public static void join(
            byte[] arr,
            Function<Byte, String> toStringFunc,
            StringBuffer outputStringBuffer) {
        join(arr, DEFAULT_SEPERATOR, toStringFunc, outputStringBuffer);
    }
    
    public static void join(
            byte[] arr,
            String seperator,
            Function<Byte, String> toStringFunc,
            StringBuilder outputStringBuilder) {
        Arguments.mustNotBeNull("arr", arr);
        Arguments.mustNotBeNull("outputStringBuilder", outputStringBuilder);
        if (seperator == null || seperator.isEmpty()) {
            if (toStringFunc == null) {
                for (byte element : arr) {
                    outputStringBuilder.append(element);
                }
            } else {
                for (byte element : arr) {
                    outputStringBuilder.append(toStringFunc.apply(element));
                }
            }
        } else {
            boolean addSeperator = false;
            if (toStringFunc == null) {
                for (byte element : arr) {
                    if (addSeperator) {
                        outputStringBuilder.append(seperator);
                    } else {
                        addSeperator = true;
                    }
                    outputStringBuilder.append(element);
                }
            } else {
                for (byte element : arr) {
                    if (addSeperator) {
                        outputStringBuilder.append(seperator);
                    } else {
                        addSeperator = true;
                    }
                    outputStringBuilder.append(toStringFunc.apply(element));
                }
            }
        }
    }
    
    public static void join(
            byte[] arr,
            String seperator,
            Function<Byte, String> toStringFunc,
            StringBuffer outputStringBuffer) {
        Arguments.mustNotBeNull("arr", arr);
        Arguments.mustNotBeNull("outputStringBuffer", outputStringBuffer);
        if (seperator == null || seperator.isEmpty()) {
            if (toStringFunc == null) {
                for (byte element : arr) {
                    outputStringBuffer.append(element);
                }
            } else {
                for (byte element : arr) {
                    outputStringBuffer.append(toStringFunc.apply(element));
                }
            }
        } else {
            boolean addSeperator = false;
            if (toStringFunc == null) {
                for (byte element : arr) {
                    if (addSeperator) {
                        outputStringBuffer.append(seperator);
                    } else {
                        addSeperator = true;
                    }
                    outputStringBuffer.append(element);
                }
            } else {
                for (byte element : arr) {
                    if (addSeperator) {
                        outputStringBuffer.append(seperator);
                    } else {
                        addSeperator = true;
                    }
                    outputStringBuffer.append(toStringFunc.apply(element));
                }
            }
        }
    }
    
    public static <T> String join(
            short[] arr) {
        StringBuilder builder = new StringBuilder();
        join(arr, DEFAULT_SEPERATOR, null, builder);
        return builder.toString();
    }

    public static String join(
            short[] arr,
            String sperator) {
        StringBuilder builder = new StringBuilder();
        join(arr, sperator, null, builder);
        return builder.toString();
    }
    
    public static String join(
            short[] arr,
            Function<Short, String> toStringFunc) {
        StringBuilder builder = new StringBuilder();
        join(arr, DEFAULT_SEPERATOR, toStringFunc, builder);
        return builder.toString();
    }

    public static String join(
            short[] arr,
            String seperator,
            Function<Short, String> toStringFunc) {
        StringBuilder builder = new StringBuilder();
        join(arr, seperator, toStringFunc, builder);
        return builder.toString();
    }
    
    public static void join(
            short[] arr,
            StringBuilder outputStringBuilder) {
        join(arr, DEFAULT_SEPERATOR, null, outputStringBuilder);
    }

    public static void join(
            short[] arr,
            String sperator,
            StringBuilder outputStringBuilder) {
        join(arr, sperator, null, outputStringBuilder);
    }
    
    public static void join(
            short[] arr,
            Function<Short, String> toStringFunc,
            StringBuilder outputStringBuilder) {
        join(arr, DEFAULT_SEPERATOR, toStringFunc, outputStringBuilder);
    }

    public static void join(
            short[] arr,
            StringBuffer outputStringBuffer) {
        join(arr, DEFAULT_SEPERATOR, null, outputStringBuffer);
    }

    public static void join(
            short[] arr,
            String sperator,
            StringBuffer outputStringBuffer) {
        join(arr, sperator, null, outputStringBuffer);
    }
    
    public static void join(
            short[] arr,
            Function<Short, String> toStringFunc,
            StringBuffer outputStringBuffer) {
        join(arr, DEFAULT_SEPERATOR, toStringFunc, outputStringBuffer);
    }
    
    public static void join(
            short[] arr,
            String seperator,
            Function<Short, String> toStringFunc,
            StringBuilder outputStringBuilder) {
        Arguments.mustNotBeNull("arr", arr);
        Arguments.mustNotBeNull("outputStringBuilder", outputStringBuilder);
        if (seperator == null || seperator.isEmpty()) {
            if (toStringFunc == null) {
                for (short element : arr) {
                    outputStringBuilder.append(element);
                }
            } else {
                for (short element : arr) {
                    outputStringBuilder.append(toStringFunc.apply(element));
                }
            }
        } else {
            boolean addSeperator = false;
            if (toStringFunc == null) {
                for (short element : arr) {
                    if (addSeperator) {
                        outputStringBuilder.append(seperator);
                    } else {
                        addSeperator = true;
                    }
                    outputStringBuilder.append(element);
                }
            } else {
                for (short element : arr) {
                    if (addSeperator) {
                        outputStringBuilder.append(seperator);
                    } else {
                        addSeperator = true;
                    }
                    outputStringBuilder.append(toStringFunc.apply(element));
                }
            }
        }
    }
    
    public static void join(
            short[] arr,
            String seperator,
            Function<Short, String> toStringFunc,
            StringBuffer outputStringBuffer) {
        Arguments.mustNotBeNull("arr", arr);
        Arguments.mustNotBeNull("outputStringBuffer", outputStringBuffer);
        if (seperator == null || seperator.isEmpty()) {
            if (toStringFunc == null) {
                for (short element : arr) {
                    outputStringBuffer.append(element);
                }
            } else {
                for (short element : arr) {
                    outputStringBuffer.append(toStringFunc.apply(element));
                }
            }
        } else {
            boolean addSeperator = false;
            if (toStringFunc == null) {
                for (short element : arr) {
                    if (addSeperator) {
                        outputStringBuffer.append(seperator);
                    } else {
                        addSeperator = true;
                    }
                    outputStringBuffer.append(element);
                }
            } else {
                for (short element : arr) {
                    if (addSeperator) {
                        outputStringBuffer.append(seperator);
                    } else {
                        addSeperator = true;
                    }
                    outputStringBuffer.append(toStringFunc.apply(element));
                }
            }
        }
    }
    
    public static <T> String join(
            int[] arr) {
        StringBuilder builder = new StringBuilder();
        join(arr, DEFAULT_SEPERATOR, null, builder);
        return builder.toString();
    }

    public static String join(
            int[] arr,
            String sperator) {
        StringBuilder builder = new StringBuilder();
        join(arr, sperator, null, builder);
        return builder.toString();
    }
    
    public static String join(
            int[] arr,
            Function<Integer, String> toStringFunc) {
        StringBuilder builder = new StringBuilder();
        join(arr, DEFAULT_SEPERATOR, toStringFunc, builder);
        return builder.toString();
    }

    public static String join(
            int[] arr,
            String seperator,
            Function<Integer, String> toStringFunc) {
        StringBuilder builder = new StringBuilder();
        join(arr, seperator, toStringFunc, builder);
        return builder.toString();
    }
    
    public static void join(
            int[] arr,
            StringBuilder outputStringBuilder) {
        join(arr, DEFAULT_SEPERATOR, null, outputStringBuilder);
    }

    public static void join(
            int[] arr,
            String sperator,
            StringBuilder outputStringBuilder) {
        join(arr, sperator, null, outputStringBuilder);
    }
    
    public static void join(
            int[] arr,
            Function<Integer, String> toStringFunc,
            StringBuilder outputStringBuilder) {
        join(arr, DEFAULT_SEPERATOR, toStringFunc, outputStringBuilder);
    }

    public static void join(
            int[] arr,
            StringBuffer outputStringBuffer) {
        join(arr, DEFAULT_SEPERATOR, null, outputStringBuffer);
    }

    public static void join(
            int[] arr,
            String sperator,
            StringBuffer outputStringBuffer) {
        join(arr, sperator, null, outputStringBuffer);
    }
    
    public static void join(
            int[] arr,
            Function<Integer, String> toStringFunc,
            StringBuffer outputStringBuffer) {
        join(arr, DEFAULT_SEPERATOR, toStringFunc, outputStringBuffer);
    }
    
    public static void join(
            int[] arr,
            String seperator,
            Function<Integer, String> toStringFunc,
            StringBuilder outputStringBuilder) {
        Arguments.mustNotBeNull("arr", arr);
        Arguments.mustNotBeNull("outputStringBuilder", outputStringBuilder);
        if (seperator == null || seperator.isEmpty()) {
            if (toStringFunc == null) {
                for (int element : arr) {
                    outputStringBuilder.append(element);
                }
            } else {
                for (int element : arr) {
                    outputStringBuilder.append(toStringFunc.apply(element));
                }
            }
        } else {
            boolean addSeperator = false;
            if (toStringFunc == null) {
                for (int element : arr) {
                    if (addSeperator) {
                        outputStringBuilder.append(seperator);
                    } else {
                        addSeperator = true;
                    }
                    outputStringBuilder.append(element);
                }
            } else {
                for (int element : arr) {
                    if (addSeperator) {
                        outputStringBuilder.append(seperator);
                    } else {
                        addSeperator = true;
                    }
                    outputStringBuilder.append(toStringFunc.apply(element));
                }
            }
        }
    }
    
    public static void join(
            int[] arr,
            String seperator,
            Function<Integer, String> toStringFunc,
            StringBuffer outputStringBuffer) {
        Arguments.mustNotBeNull("arr", arr);
        Arguments.mustNotBeNull("outputStringBuffer", outputStringBuffer);
        if (seperator == null || seperator.isEmpty()) {
            if (toStringFunc == null) {
                for (int element : arr) {
                    outputStringBuffer.append(element);
                }
            } else {
                for (int element : arr) {
                    outputStringBuffer.append(toStringFunc.apply(element));
                }
            }
        } else {
            boolean addSeperator = false;
            if (toStringFunc == null) {
                for (int element : arr) {
                    if (addSeperator) {
                        outputStringBuffer.append(seperator);
                    } else {
                        addSeperator = true;
                    }
                    outputStringBuffer.append(element);
                }
            } else {
                for (int element : arr) {
                    if (addSeperator) {
                        outputStringBuffer.append(seperator);
                    } else {
                        addSeperator = true;
                    }
                    outputStringBuffer.append(toStringFunc.apply(element));
                }
            }
        }
    }
    
    public static <T> String join(
            long[] arr) {
        StringBuilder builder = new StringBuilder();
        join(arr, DEFAULT_SEPERATOR, null, builder);
        return builder.toString();
    }

    public static String join(
            long[] arr,
            String sperator) {
        StringBuilder builder = new StringBuilder();
        join(arr, sperator, null, builder);
        return builder.toString();
    }
    
    public static String join(
            long[] arr,
            Function<Long, String> toStringFunc) {
        StringBuilder builder = new StringBuilder();
        join(arr, DEFAULT_SEPERATOR, toStringFunc, builder);
        return builder.toString();
    }

    public static String join(
            long[] arr,
            String seperator,
            Function<Long, String> toStringFunc) {
        StringBuilder builder = new StringBuilder();
        join(arr, seperator, toStringFunc, builder);
        return builder.toString();
    }
    
    public static void join(
            long[] arr,
            StringBuilder outputStringBuilder) {
        join(arr, DEFAULT_SEPERATOR, null, outputStringBuilder);
    }

    public static void join(
            long[] arr,
            String sperator,
            StringBuilder outputStringBuilder) {
        join(arr, sperator, null, outputStringBuilder);
    }
    
    public static void join(
            long[] arr,
            Function<Long, String> toStringFunc,
            StringBuilder outputStringBuilder) {
        join(arr, DEFAULT_SEPERATOR, toStringFunc, outputStringBuilder);
    }

    public static void join(
            long[] arr,
            StringBuffer outputStringBuffer) {
        join(arr, DEFAULT_SEPERATOR, null, outputStringBuffer);
    }

    public static void join(
            long[] arr,
            String sperator,
            StringBuffer outputStringBuffer) {
        join(arr, sperator, null, outputStringBuffer);
    }
    
    public static void join(
            long[] arr,
            Function<Long, String> toStringFunc,
            StringBuffer outputStringBuffer) {
        join(arr, DEFAULT_SEPERATOR, toStringFunc, outputStringBuffer);
    }
    
    public static void join(
            long[] arr,
            String seperator,
            Function<Long, String> toStringFunc,
            StringBuilder outputStringBuilder) {
        Arguments.mustNotBeNull("arr", arr);
        Arguments.mustNotBeNull("outputStringBuilder", outputStringBuilder);
        if (seperator == null || seperator.isEmpty()) {
            if (toStringFunc == null) {
                for (long element : arr) {
                    outputStringBuilder.append(element);
                }
            } else {
                for (long element : arr) {
                    outputStringBuilder.append(toStringFunc.apply(element));
                }
            }
        } else {
            boolean addSeperator = false;
            if (toStringFunc == null) {
                for (long element : arr) {
                    if (addSeperator) {
                        outputStringBuilder.append(seperator);
                    } else {
                        addSeperator = true;
                    }
                    outputStringBuilder.append(element);
                }
            } else {
                for (long element : arr) {
                    if (addSeperator) {
                        outputStringBuilder.append(seperator);
                    } else {
                        addSeperator = true;
                    }
                    outputStringBuilder.append(toStringFunc.apply(element));
                }
            }
        }
    }
    
    public static void join(
            long[] arr,
            String seperator,
            Function<Long, String> toStringFunc,
            StringBuffer outputStringBuffer) {
        Arguments.mustNotBeNull("arr", arr);
        Arguments.mustNotBeNull("outputStringBuffer", outputStringBuffer);
        if (seperator == null || seperator.isEmpty()) {
            if (toStringFunc == null) {
                for (long element : arr) {
                    outputStringBuffer.append(element);
                }
            } else {
                for (long element : arr) {
                    outputStringBuffer.append(toStringFunc.apply(element));
                }
            }
        } else {
            boolean addSeperator = false;
            if (toStringFunc == null) {
                for (long element : arr) {
                    if (addSeperator) {
                        outputStringBuffer.append(seperator);
                    } else {
                        addSeperator = true;
                    }
                    outputStringBuffer.append(element);
                }
            } else {
                for (long element : arr) {
                    if (addSeperator) {
                        outputStringBuffer.append(seperator);
                    } else {
                        addSeperator = true;
                    }
                    outputStringBuffer.append(toStringFunc.apply(element));
                }
            }
        }
    }
    
    public static <T> String join(
            float[] arr) {
        StringBuilder builder = new StringBuilder();
        join(arr, DEFAULT_SEPERATOR, null, builder);
        return builder.toString();
    }

    public static String join(
            float[] arr,
            String sperator) {
        StringBuilder builder = new StringBuilder();
        join(arr, sperator, null, builder);
        return builder.toString();
    }
    
    public static String join(
            float[] arr,
            Function<Float, String> toStringFunc) {
        StringBuilder builder = new StringBuilder();
        join(arr, DEFAULT_SEPERATOR, toStringFunc, builder);
        return builder.toString();
    }

    public static String join(
            float[] arr,
            String seperator,
            Function<Float, String> toStringFunc) {
        StringBuilder builder = new StringBuilder();
        join(arr, seperator, toStringFunc, builder);
        return builder.toString();
    }
    
    public static void join(
            float[] arr,
            StringBuilder outputStringBuilder) {
        join(arr, DEFAULT_SEPERATOR, null, outputStringBuilder);
    }

    public static void join(
            float[] arr,
            String sperator,
            StringBuilder outputStringBuilder) {
        join(arr, sperator, null, outputStringBuilder);
    }
    
    public static void join(
            float[] arr,
            Function<Float, String> toStringFunc,
            StringBuilder outputStringBuilder) {
        join(arr, DEFAULT_SEPERATOR, toStringFunc, outputStringBuilder);
    }

    public static void join(
            float[] arr,
            StringBuffer outputStringBuffer) {
        join(arr, DEFAULT_SEPERATOR, null, outputStringBuffer);
    }

    public static void join(
            float[] arr,
            String sperator,
            StringBuffer outputStringBuffer) {
        join(arr, sperator, null, outputStringBuffer);
    }
    
    public static void join(
            float[] arr,
            Function<Float, String> toStringFunc,
            StringBuffer outputStringBuffer) {
        join(arr, DEFAULT_SEPERATOR, toStringFunc, outputStringBuffer);
    }
    
    public static void join(
            float[] arr,
            String seperator,
            Function<Float, String> toStringFunc,
            StringBuilder outputStringBuilder) {
        Arguments.mustNotBeNull("arr", arr);
        Arguments.mustNotBeNull("outputStringBuilder", outputStringBuilder);
        if (seperator == null || seperator.isEmpty()) {
            if (toStringFunc == null) {
                for (float element : arr) {
                    outputStringBuilder.append(element);
                }
            } else {
                for (float element : arr) {
                    outputStringBuilder.append(toStringFunc.apply(element));
                }
            }
        } else {
            boolean addSeperator = false;
            if (toStringFunc == null) {
                for (float element : arr) {
                    if (addSeperator) {
                        outputStringBuilder.append(seperator);
                    } else {
                        addSeperator = true;
                    }
                    outputStringBuilder.append(element);
                }
            } else {
                for (float element : arr) {
                    if (addSeperator) {
                        outputStringBuilder.append(seperator);
                    } else {
                        addSeperator = true;
                    }
                    outputStringBuilder.append(toStringFunc.apply(element));
                }
            }
        }
    }
    
    public static void join(
            float[] arr,
            String seperator,
            Function<Float, String> toStringFunc,
            StringBuffer outputStringBuffer) {
        Arguments.mustNotBeNull("arr", arr);
        Arguments.mustNotBeNull("outputStringBuffer", outputStringBuffer);
        if (seperator == null || seperator.isEmpty()) {
            if (toStringFunc == null) {
                for (float element : arr) {
                    outputStringBuffer.append(element);
                }
            } else {
                for (float element : arr) {
                    outputStringBuffer.append(toStringFunc.apply(element));
                }
            }
        } else {
            boolean addSeperator = false;
            if (toStringFunc == null) {
                for (float element : arr) {
                    if (addSeperator) {
                        outputStringBuffer.append(seperator);
                    } else {
                        addSeperator = true;
                    }
                    outputStringBuffer.append(element);
                }
            } else {
                for (float element : arr) {
                    if (addSeperator) {
                        outputStringBuffer.append(seperator);
                    } else {
                        addSeperator = true;
                    }
                    outputStringBuffer.append(toStringFunc.apply(element));
                }
            }
        }
    }
    
    public static <T> String join(
            double[] arr) {
        StringBuilder builder = new StringBuilder();
        join(arr, DEFAULT_SEPERATOR, null, builder);
        return builder.toString();
    }

    public static String join(
            double[] arr,
            String sperator) {
        StringBuilder builder = new StringBuilder();
        join(arr, sperator, null, builder);
        return builder.toString();
    }
    
    public static String join(
            double[] arr,
            Function<Double, String> toStringFunc) {
        StringBuilder builder = new StringBuilder();
        join(arr, DEFAULT_SEPERATOR, toStringFunc, builder);
        return builder.toString();
    }

    public static String join(
            double[] arr,
            String seperator,
            Function<Double, String> toStringFunc) {
        StringBuilder builder = new StringBuilder();
        join(arr, seperator, toStringFunc, builder);
        return builder.toString();
    }
    
    public static void join(
            double[] arr,
            StringBuilder outputStringBuilder) {
        join(arr, DEFAULT_SEPERATOR, null, outputStringBuilder);
    }

    public static void join(
            double[] arr,
            String sperator,
            StringBuilder outputStringBuilder) {
        join(arr, sperator, null, outputStringBuilder);
    }
    
    public static void join(
            double[] arr,
            Function<Double, String> toStringFunc,
            StringBuilder outputStringBuilder) {
        join(arr, DEFAULT_SEPERATOR, toStringFunc, outputStringBuilder);
    }

    public static void join(
            double[] arr,
            StringBuffer outputStringBuffer) {
        join(arr, DEFAULT_SEPERATOR, null, outputStringBuffer);
    }

    public static void join(
            double[] arr,
            String sperator,
            StringBuffer outputStringBuffer) {
        join(arr, sperator, null, outputStringBuffer);
    }
    
    public static void join(
            double[] arr,
            Function<Double, String> toStringFunc,
            StringBuffer outputStringBuffer) {
        join(arr, DEFAULT_SEPERATOR, toStringFunc, outputStringBuffer);
    }
    
    public static void join(
            double[] arr,
            String seperator,
            Function<Double, String> toStringFunc,
            StringBuilder outputStringBuilder) {
        Arguments.mustNotBeNull("arr", arr);
        Arguments.mustNotBeNull("outputStringBuilder", outputStringBuilder);
        if (seperator == null || seperator.isEmpty()) {
            if (toStringFunc == null) {
                for (double element : arr) {
                    outputStringBuilder.append(element);
                }
            } else {
                for (double element : arr) {
                    outputStringBuilder.append(toStringFunc.apply(element));
                }
            }
        } else {
            boolean addSeperator = false;
            if (toStringFunc == null) {
                for (double element : arr) {
                    if (addSeperator) {
                        outputStringBuilder.append(seperator);
                    } else {
                        addSeperator = true;
                    }
                    outputStringBuilder.append(element);
                }
            } else {
                for (double element : arr) {
                    if (addSeperator) {
                        outputStringBuilder.append(seperator);
                    } else {
                        addSeperator = true;
                    }
                    outputStringBuilder.append(toStringFunc.apply(element));
                }
            }
        }
    }
    
    public static void join(
            double[] arr,
            String seperator,
            Function<Double, String> toStringFunc,
            StringBuffer outputStringBuffer) {
        Arguments.mustNotBeNull("arr", arr);
        Arguments.mustNotBeNull("outputStringBuffer", outputStringBuffer);
        if (seperator == null || seperator.isEmpty()) {
            if (toStringFunc == null) {
                for (double element : arr) {
                    outputStringBuffer.append(element);
                }
            } else {
                for (double element : arr) {
                    outputStringBuffer.append(toStringFunc.apply(element));
                }
            }
        } else {
            boolean addSeperator = false;
            if (toStringFunc == null) {
                for (double element : arr) {
                    if (addSeperator) {
                        outputStringBuffer.append(seperator);
                    } else {
                        addSeperator = true;
                    }
                    outputStringBuffer.append(element);
                }
            } else {
                for (double element : arr) {
                    if (addSeperator) {
                        outputStringBuffer.append(seperator);
                    } else {
                        addSeperator = true;
                    }
                    outputStringBuffer.append(toStringFunc.apply(element));
                }
            }
        }
    }
}
