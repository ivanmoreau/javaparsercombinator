import java.math.BigDecimal;
import java.util.Optional;

/**
 * GRAMMAR
 * expr : sum;
 * sum : product (('+' | '-') product)*;
 * product : value (('*' | '/') value)*;
 * value : NUMBER | '(' expr ')';
 * NUMBER : FLOAT | INT;
 * INT : '-' NAT;
 * NAT : [0-9]+;
 * FLOAT : INT FLOAT_P | '-' FLOAT_P | FLOAT_P;
 * FLOAT_P : '.' [0-9]*;
 */

public class Parser {
    /**
     * Main hall of the side effects.
     * Spooky.
     */
    public static void main(String[] args) {
        //System.out.println(expr().apply("8/-2*(2+2) * .5"));
        impure_tester();
    }

    /**
     * This function is impure and it has
     * side effects. For testing purposes
     * only. Should be avoided in production.
     */
    static void impure_tester() {
        java.util.Scanner scanner = new java.util.Scanner(System.in);
        System.out.println("Write <_> to exit. (Without <>)");
        for(;;){
            String tex = scanner.nextLine();
            switch (tex) {
                case "_": 
                    scanner.close();
                    break;
                default:
                    var result = expr().apply(tex.replace("\n", ""));
                    System.out.println(result);
                    continue;
                }
            break;
        }
        
    }

    /**
     * expr :: String -> (Maybe Number, String) <hr>
     * This is the main parser and it should be used.
     * Consumes a String, returns a Tuple<Optional<BigInteger>, String>.
     * If the parser fails to parse the input, it will return
     * either an empty Optional or a non-empty String.
     * If a non-empty String is returned, it will contain
     * the unparsed character as the first character of that String.
     */
    public static F<String, T<Optional<BigDecimal>, String>> expr() {
        return new F<String, T<Optional<BigDecimal>, String>>() {
            public T<Optional<BigDecimal>, String> apply(String str) {
                return sum().apply(str.replace(" ", ""));
            }
        };
    }

    /**
     * sum :: String -> (Maybe Number, String)
     */
    public static F<String, T<Optional<BigDecimal>, String>> sum() {
        return new F<String, T<Optional<BigDecimal>, String>>() {
            public T<Optional<BigDecimal>, String> apply(String str) {
                T<Optional<BigDecimal>, String> tmp0 = product().apply(str);
                if (tmp0._0.isPresent()) {
                    return sum_().apply(tmp0._1).apply(tmp0._0.get());
                }
                return new T<Optional<BigDecimal>, String>(Optional.empty(), str);
            }
        };
    }

    /**
     * sum internal, Kleene star
     */
    public static F<String, F<BigDecimal, T<Optional<BigDecimal>, String>>> sum_() {
        return new F<String, F<BigDecimal, T<Optional<BigDecimal>, String>>>() {
            public F<BigDecimal, T<Optional<BigDecimal>, String>> apply(String str) {
                return new F<BigDecimal, T<Optional<BigDecimal>, String>>() {
                    public T<Optional<BigDecimal>, String> apply(BigDecimal num) {
                        T<Optional<Character>, String> t;
                        T<Optional<Character>, String> t1 = char_().apply('+').apply(str);
                        if (t1._0.isEmpty()) {
                            t = char_().apply('-').apply(str);
                        } else {
                            t = t1;
                        }
                        if (t._0.isPresent()) {
                            T<Optional<BigDecimal>, String> tmp2 = product().apply(t._1);
                            if (tmp2._0.isPresent()) {
                                BigDecimal val;
                                if (t._0.get().equals('+')) {
                                    val = num.add(tmp2._0.get());
                                } else {
                                    val = num.subtract(tmp2._0.get());
                                }
                                return sum_().apply(tmp2._1).apply(val);
                            }
                        }
                        return new T<Optional<BigDecimal>, String>(Optional.of(num), str);
                    }
                };
            }
        };
    }

    /**
     * product :: String -> (Maybe Number, String)
     */
    public static F<String, T<Optional<BigDecimal>, String>> product() {
        return new F<String, T<Optional<BigDecimal>, String>>() {
            public T<Optional<BigDecimal>, String> apply(String str) {
                T<Optional<BigDecimal>, String> tmp0 = value().apply(str);
                if (tmp0._0.isPresent()) {
                    return product_().apply(tmp0._1).apply(tmp0._0.get());
                }
                return new T<Optional<BigDecimal>, String>(Optional.empty(), str);
            }
        };
    }

    /**
     * product internal, Kleene star
     */
    public static F<String, F<BigDecimal, T<Optional<BigDecimal>, String>>> product_() {
        return new F<String, F<BigDecimal, T<Optional<BigDecimal>, String>>>() {
            public F<BigDecimal, T<Optional<BigDecimal>, String>> apply(String str) {
                return new F<BigDecimal, T<Optional<BigDecimal>, String>>() {
                    public T<Optional<BigDecimal>, String> apply(BigDecimal num) {
                        T<Optional<Character>, String> t;
                        T<Optional<Character>, String> t1 = char_().apply('*').apply(str);
                        if (t1._0.isEmpty()) {
                            t = char_().apply('/').apply(str);
                        } else {
                            t = t1;
                        }
                        if (t._0.isPresent()) {
                            //T<Optional<Integer>, String> tmp2 = value().apply(t._1);
                            T<Optional<BigDecimal>, String> tmp2 = value().apply(t._1);
                            if (tmp2._0.isPresent()) {
                                BigDecimal val;
                                if (t._0.get().equals('*')) {
                                    val = num.multiply(tmp2._0.get());
                                } else {
                                    val = num.divide(tmp2._0.get());
                                }
                                return product_().apply(tmp2._1).apply(val);
                            }
                        }
                        return new T<Optional<BigDecimal>, String>(Optional.of(num), str);
                    }
                };
            }
        };
    }

    /**
     * value :: String -> (Maybe Number, String)
     */
    public static F<String, T<Optional<BigDecimal>, String>> value() {
        return new F<String, T<Optional<BigDecimal>, String>>() {
            public T<Optional<BigDecimal>, String> apply(String str) {
                T<Optional<BigDecimal>, String> tmp0 = number().apply(str);
                if (tmp0._0.isPresent()) {
                    return tmp0;
                } else {
                    T<Optional<Character>, String> t = char_().apply('(').apply(str);
                    if (t._0.isPresent()) {
                        T<Optional<BigDecimal>, String> p = expr().apply(t._1);
                        if (p._0.isPresent()) {
                            T<Optional<Character>, String> g = char_().apply(')').apply(p._1);
                            if (g._0.isPresent()) {
                                return new T<Optional<BigDecimal>, String>(
                                    Optional.of(p._0.get()),
                                    g._1
                                );
                            }
                        }
                    }
                }
                return new T<Optional<BigDecimal>, String>(Optional.empty(), str);
            }
        };
    }

    /**
     * number :: String -> (Maybe Number, String)
     */
    public static F<String, T<Optional<BigDecimal>, String>> number() {
        return new F<String, T<Optional<BigDecimal>, String>>() {
            public T<Optional<BigDecimal>, String> apply(String str) {
                T<Optional<BigDecimal>, String> tmp0 = float_p_().apply(str);
                if (tmp0._0.isPresent()) {
                    return tmp0;
                } else {
                    T<Optional<BigDecimal>, String> tmp1 = int_().apply(str);
                    if (tmp1._0.isPresent()) {
                        return tmp1;
                    }
                }
                return new T<Optional<BigDecimal>, String>(Optional.empty(), str);
            }
        };
    }

    /**
     * float_ :: String -> (Maybe Number, String)
     */
    public static F<String, T<Optional<BigDecimal>, String>> float_() {
        return new F<String, T<Optional<BigDecimal>, String>>() {
            public T<Optional<BigDecimal>, String> apply(String str) {
                T<Optional<BigDecimal>, String> tmp0 = int_().apply(str);
                if (tmp0._0.isPresent()) {
                    Double tmp;
                    if (tmp0._0.get().floatValue() < 0.0) {
                        tmp = -1.0;
                    } else {
                        tmp = 1.0;
                    }
                    T<Optional<BigDecimal>, String> tmp00 = float_p_().apply(tmp0._1);
                    if (tmp00._0.isPresent()) {
                        BigDecimal val = tmp00._0.get().add(tmp00._0.get().multiply(new BigDecimal(tmp)));
                        return new T<Optional<BigDecimal>, String>(Optional.of(val), tmp00._1);
                    }
                } else {
                    T<Optional<Character>, String> tmp1 = char_().apply('-').apply(str);
                    Double tmp;
                    if (tmp1._0.isPresent()) {
                        tmp = -1.0;
                    } else {
                        tmp = 1.0;
                    }
                    T<Optional<BigDecimal>, String> tmp2 = float_p_().apply(tmp1._1);
                    if (tmp2._0.isPresent()) {
                        BigDecimal val = tmp2._0.get().multiply(new BigDecimal(tmp));
                        return new T<Optional<BigDecimal>, String>(Optional.of(val), tmp2._1);
                    }
                }
                return new T<Optional<BigDecimal>, String>(Optional.empty(), str);
            }
        };
    }

    /**
     * float_p_ :: String -> (Maybe Number, String)
     */
    public static F<String, T<Optional<BigDecimal>, String>> float_p_() {
        return new F<String, T<Optional<BigDecimal>, String>>() {
            public T<Optional<BigDecimal>, String> apply(String str) {
                T<Optional<Character>, String> tmp0 = char_().apply('.').apply(str);
                if (tmp0._0.isPresent()) {
                    T<String, String> tmp = parse_some(digit()).apply(tmp0._1);
                    Double val = tmp._0.isEmpty() ? 0 : Double.valueOf("0" + tmp0._0.get() + tmp._0);
                    return new T<Optional<BigDecimal>, String>(Optional.of(new BigDecimal(val)), tmp._1);
                }
                return new T<Optional<BigDecimal>, String>(Optional.empty(), str);
            }
        };
    }

    /**
     * int_ :: String -> (Maybe Number, String)
     */
    public static F<String, T<Optional<BigDecimal>, String>> int_() {
        return new F<String, T<Optional<BigDecimal>, String>>() {
            public T<Optional<BigDecimal>, String> apply(String str) {
                T<Optional<Character>, String> tmp0 = char_().apply('-').apply(str);
                String str0;
                int ret;
                if (tmp0._0.isPresent()) {
                    str0 = tmp0._1;
                    ret = -1;
                } else {
                    str0 = str;
                    ret = 1;
                }
                T<String, String> tmp = parse_some(digit()).apply(str0);
                Optional<BigDecimal> val = tmp._0.isEmpty() ? Optional.empty() 
                    : Optional.of(new BigDecimal(ret * Integer.valueOf(tmp._0)));
                return new T<Optional<BigDecimal>, String>(val, tmp._1);
            }
        };
    }

    /**
     * nat :: String -> (Maybe Integer, String)
     */
    public static F<String, T<Optional<Integer>, String>> nat() {
        return new F<String, T<Optional<Integer>, String>>() {
            public T<Optional<Integer>, String> apply(String str) {
                T<String, String> tmp = parse_some(digit()).apply(str);
                Optional<Integer> val = tmp._0.isEmpty() ? Optional.empty() : Optional.of(Integer.valueOf(tmp._0));
                return new T<Optional<Integer>, String>(val, tmp._1);
            }
        };
    }

    /**
     * parse_some :: (String -> (Maybe Character, String)) -> (String -> (String, String))
     */
    public static F<String, T<String, String>> parse_some(F<String, T<Optional<Character>, String>> F) {
        return new F<String, T<String, String>>() {
            public T<String, String> apply(String str) {
                T<Optional<Character>, String> tmp = F.apply(str);
                if (tmp._0.isEmpty()) {
                    return new T<String, String>("", str);
                } else {
                    T<String, String> tmp2 = parse_some(F).apply(substring(str));
                    return new T<String, String>(tmp._0.get() + tmp2._0, tmp2._1);
                }
            }
        };
    }

    /**
     * char_ :: Character -> (String -> (Maybe Character, String))
     */
    public static F<Character, F<String, T<Optional<Character>, String>>> char_() {
        return new F<Character, F<String, T<Optional<Character>, String>>>() {
            public F<String, T<Optional<Character>, String>> apply(Character c) {
                return satisfies(new F<Character, Boolean>(){
                    public Boolean apply(Character c_) {
                        return c == c_;
                    }
                });
            }
        };
    }

    /**
     * digit :: String -> (Maybe Character, String)
     */
    public static F<String, T<Optional<Character>, String>> digit() {
        return new F<String, T<Optional<Character>, String>>() {
            public T<Optional<Character>, String> apply(String str) {
                return satisfies(is_digit()).apply(str);
            }
        };
    }
    
    /**
     * isDigit :: Character -> Boolean
     */
    public static F<Character, Boolean> is_digit() {
        return new F<Character, Boolean>() {
            public Boolean apply(Character c) {
                return Character.isDigit(c);
            }
        };
    }

    /**
     * satisfies :: (Character -> Boolean) -> (String -> (Maybe Character, String))
     */
    public static F<String, T<Optional<Character>, String>> satisfies(F<Character, Boolean> F) {
        return new F<String, T<Optional<Character>, String>>() {
            public T<Optional<Character>, String> apply(String str) {
                if (str.isEmpty()) {
                    return new T<Optional<Character>, String>(Optional.empty(), "");
                } else {
                    if (F.apply(str.charAt(0))) {
                        return new T<Optional<Character>, String>(Optional.of(str.charAt(0)), substring(str));
                    } else {
                        return new T<Optional<Character>, String>(Optional.empty(), str);
                    }
                }
            }
        };
    }

    /**
     * substring :: String -> String
     * this is a wrapper impure -> "pure"
     * @return a substring from 1 to ... or "".
     */
    public static String substring(String str) {
        try {
            return str.substring(1);
        } catch (Exception e) {
            return "";
        }
    }
}

/**
 * (_0, _1)
 */
class T<A, B> {
    public final A _0;
    public final B _1;
    T(A _0, B _1) {
        this._0 = _0; this._1 = _1;
    }
    public String toString() {
        return "(" + _0 + ", \"" + _1 + "\")";
    }
}


/**
 * F :: Q -> R
 */
interface F<Q, R> {
    public R apply(Q parameter);
}