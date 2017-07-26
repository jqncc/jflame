package org.jflame.db.id;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public final class IdGeneratorHelper {

    public static IntegralDataTypeHolder getIntegralDataTypeHolder(Class<?> integralType) {
        if (integralType == Long.class || integralType == Integer.class || integralType == Short.class) {
            return new BasicHolder(integralType);
        } else if (integralType == BigInteger.class) {
            return new BigIntegerHolder();
        } else if (integralType == BigDecimal.class) {
            return new BigDecimalHolder();
        } else {
            throw new IdGenerationException("Unknown integral data type for ids : " + integralType.getName());
        }
    }

    public static long extractLong(IntegralDataTypeHolder holder) {
        if (holder.getClass() == BasicHolder.class) {
            ((BasicHolder) holder).checkInitialized();
            return ((BasicHolder) holder).value;
        } else if (holder.getClass() == BigIntegerHolder.class) {
            ((BigIntegerHolder) holder).checkInitialized();
            return ((BigIntegerHolder) holder).value.longValue();
        } else if (holder.getClass() == BigDecimalHolder.class) {
            ((BigDecimalHolder) holder).checkInitialized();
            return ((BigDecimalHolder) holder).value.longValue();
        }
        throw new IdGenerationException("Unknown IntegralDataTypeHolder impl [" + holder + "]");
    }

    public static BigInteger extractBigInteger(IntegralDataTypeHolder holder) {
        if (holder.getClass() == BasicHolder.class) {
            ((BasicHolder) holder).checkInitialized();
            return BigInteger.valueOf(((BasicHolder) holder).value);
        } else if (holder.getClass() == BigIntegerHolder.class) {
            ((BigIntegerHolder) holder).checkInitialized();
            return ((BigIntegerHolder) holder).value;
        } else if (holder.getClass() == BigDecimalHolder.class) {
            ((BigDecimalHolder) holder).checkInitialized();
            // scale should already be set...
            return ((BigDecimalHolder) holder).value.toBigInteger();
        }
        throw new IdGenerationException("Unknown IntegralDataTypeHolder impl [" + holder + "]");
    }

    public static BigDecimal extractBigDecimal(IntegralDataTypeHolder holder) {
        if (holder.getClass() == BasicHolder.class) {
            ((BasicHolder) holder).checkInitialized();
            return BigDecimal.valueOf(((BasicHolder) holder).value);
        } else if (holder.getClass() == BigIntegerHolder.class) {
            ((BigIntegerHolder) holder).checkInitialized();
            return new BigDecimal(((BigIntegerHolder) holder).value);
        } else if (holder.getClass() == BigDecimalHolder.class) {
            ((BigDecimalHolder) holder).checkInitialized();
            // scale should already be set...
            return ((BigDecimalHolder) holder).value;
        }
        throw new IdGenerationException("Unknown IntegralDataTypeHolder impl [" + holder + "]");
    }

    public static class BasicHolder implements IntegralDataTypeHolder {
        private static final long serialVersionUID = 1L;
        private final Class<?> exactType;
        private long value = Long.MIN_VALUE;

        public BasicHolder(Class<?> exactType) {
            this.exactType = exactType;
            if (exactType != Long.class && exactType != Integer.class && exactType != Short.class) {
                throw new IdGenerationException("Invalid type for basic integral holder : " + exactType);
            }
        }

        public long getActualLongValue() {
            return value;
        }

        public IntegralDataTypeHolder initialize(long value) {
            this.value = value;
            return this;
        }

        public IntegralDataTypeHolder initialize(ResultSet resultSet, long defaultValue) throws SQLException {
            long value = resultSet.getLong(1);
            if (resultSet.wasNull()) {
                value = defaultValue;
            }
            return initialize(value);
        }

        public void bind(PreparedStatement preparedStatement, int position) throws SQLException {
            preparedStatement.setLong(position, value);
        }

        public IntegralDataTypeHolder increment() {
            checkInitialized();
            value++;
            return this;
        }

        private void checkInitialized() {
            if (value == Long.MIN_VALUE) {
                throw new IdGenerationException("integral holder was not initialized");
            }
        }

        public IntegralDataTypeHolder add(long addend) {
            checkInitialized();
            value += addend;
            return this;
        }

        public IntegralDataTypeHolder decrement() {
            checkInitialized();
            value--;
            return this;
        }

        public IntegralDataTypeHolder subtract(long subtrahend) {
            checkInitialized();
            value -= subtrahend;
            return this;
        }

        public IntegralDataTypeHolder multiplyBy(IntegralDataTypeHolder factor) {
            return multiplyBy(extractLong(factor));
        }

        public IntegralDataTypeHolder multiplyBy(long factor) {
            checkInitialized();
            value *= factor;
            return this;
        }

        public boolean eq(IntegralDataTypeHolder other) {
            return eq(extractLong(other));
        }

        public boolean eq(long value) {
            checkInitialized();
            return this.value == value;
        }

        public boolean lt(IntegralDataTypeHolder other) {
            return lt(extractLong(other));
        }

        public boolean lt(long value) {
            checkInitialized();
            return this.value < value;
        }

        public boolean gt(IntegralDataTypeHolder other) {
            return gt(extractLong(other));
        }

        public boolean gt(long value) {
            checkInitialized();
            return this.value > value;
        }

        public IntegralDataTypeHolder copy() {
            BasicHolder copy = new BasicHolder(exactType);
            copy.value = value;
            return copy;
        }

        public Number makeValue() {
            checkInitialized();
            if (exactType == Long.class) {
                return value;
            } else if (exactType == Integer.class) {
                return (int) value;
            } else {
                return (short) value;
            }
        }

        public Number makeValueThenIncrement() {
            final Number result = makeValue();
            value++;
            return result;
        }

        public Number makeValueThenAdd(long addend) {
            final Number result = makeValue();
            value += addend;
            return result;
        }

        public String toString() {
            return "BasicHolder[" + exactType.getName() + "[" + value + "]]";
        }

        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            BasicHolder that = (BasicHolder) o;

            return value == that.value;
        }

        public int hashCode() {
            return (int) (value ^ (value >>> 32));
        }
    }

    public static class BigIntegerHolder implements IntegralDataTypeHolder {

        private static final long serialVersionUID = 1L;
        private BigInteger value;

        public IntegralDataTypeHolder initialize(long value) {
            this.value = BigInteger.valueOf(value);
            return this;
        }

        public IntegralDataTypeHolder initialize(ResultSet resultSet, long defaultValue) throws SQLException {
            final BigDecimal rsValue = resultSet.getBigDecimal(1);
            if (resultSet.wasNull()) {
                return initialize(defaultValue);
            }
            this.value = rsValue.setScale(0, BigDecimal.ROUND_UNNECESSARY).toBigInteger();
            return this;
        }

        public void bind(PreparedStatement preparedStatement, int position) throws SQLException {
            preparedStatement.setBigDecimal(position, new BigDecimal(value));
        }

        public IntegralDataTypeHolder increment() {
            checkInitialized();
            value = value.add(BigInteger.ONE);
            return this;
        }

        private void checkInitialized() {
            if (value == null) {
                throw new IdGenerationException("integral holder was not initialized");
            }
        }

        public IntegralDataTypeHolder add(long increment) {
            checkInitialized();
            value = value.add(BigInteger.valueOf(increment));
            return this;
        }

        public IntegralDataTypeHolder decrement() {
            checkInitialized();
            value = value.subtract(BigInteger.ONE);
            return this;
        }

        public IntegralDataTypeHolder subtract(long subtrahend) {
            checkInitialized();
            value = value.subtract(BigInteger.valueOf(subtrahend));
            return this;
        }

        public IntegralDataTypeHolder multiplyBy(IntegralDataTypeHolder factor) {
            checkInitialized();
            value = value.multiply(extractBigInteger(factor));
            return this;
        }

        public IntegralDataTypeHolder multiplyBy(long factor) {
            checkInitialized();
            value = value.multiply(BigInteger.valueOf(factor));
            return this;
        }

        public boolean eq(IntegralDataTypeHolder other) {
            checkInitialized();
            return value.compareTo(extractBigInteger(other)) == 0;
        }

        public boolean eq(long value) {
            checkInitialized();
            return this.value.compareTo(BigInteger.valueOf(value)) == 0;
        }

        public boolean lt(IntegralDataTypeHolder other) {
            checkInitialized();
            return value.compareTo(extractBigInteger(other)) < 0;
        }

        public boolean lt(long value) {
            checkInitialized();
            return this.value.compareTo(BigInteger.valueOf(value)) < 0;
        }

        public boolean gt(IntegralDataTypeHolder other) {
            checkInitialized();
            return value.compareTo(extractBigInteger(other)) > 0;
        }

        public boolean gt(long value) {
            checkInitialized();
            return this.value.compareTo(BigInteger.valueOf(value)) > 0;
        }

        public IntegralDataTypeHolder copy() {
            BigIntegerHolder copy = new BigIntegerHolder();
            copy.value = value;
            return copy;
        }

        public Number makeValue() {
            checkInitialized();
            return value;
        }

        public Number makeValueThenIncrement() {
            final Number result = makeValue();
            value = value.add(BigInteger.ONE);
            return result;
        }

        public Number makeValueThenAdd(long addend) {
            final Number result = makeValue();
            value = value.add(BigInteger.valueOf(addend));
            return result;
        }

        public String toString() {
            return "BigIntegerHolder[" + value + "]";
        }

        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            BigIntegerHolder that = (BigIntegerHolder) o;

            return this.value == null ? that.value == null : value.equals(that.value);
        }

        public int hashCode() {
            return value != null ? value.hashCode() : 0;
        }
    }

    public static class BigDecimalHolder implements IntegralDataTypeHolder {
        private static final long serialVersionUID = 1L;
        private BigDecimal value;

        public IntegralDataTypeHolder initialize(long value) {
            this.value = BigDecimal.valueOf(value);
            return this;
        }

        public IntegralDataTypeHolder initialize(ResultSet resultSet, long defaultValue) throws SQLException {
            final BigDecimal rsValue = resultSet.getBigDecimal(1);
            if (resultSet.wasNull()) {
                return initialize(defaultValue);
            }
            this.value = rsValue.setScale(0, BigDecimal.ROUND_UNNECESSARY);
            return this;
        }

        public void bind(PreparedStatement preparedStatement, int position) throws SQLException {
            preparedStatement.setBigDecimal(position, value);
        }

        public IntegralDataTypeHolder increment() {
            checkInitialized();
            value = value.add(BigDecimal.ONE);
            return this;
        }

        private void checkInitialized() {
            if (value == null) {
                throw new IdGenerationException("integral holder was not initialized");
            }
        }

        public IntegralDataTypeHolder add(long increment) {
            checkInitialized();
            value = value.add(BigDecimal.valueOf(increment));
            return this;
        }

        public IntegralDataTypeHolder decrement() {
            checkInitialized();
            value = value.subtract(BigDecimal.ONE);
            return this;
        }

        public IntegralDataTypeHolder subtract(long subtrahend) {
            checkInitialized();
            value = value.subtract(BigDecimal.valueOf(subtrahend));
            return this;
        }

        public IntegralDataTypeHolder multiplyBy(IntegralDataTypeHolder factor) {
            checkInitialized();
            value = value.multiply(extractBigDecimal(factor));
            return this;
        }

        public IntegralDataTypeHolder multiplyBy(long factor) {
            checkInitialized();
            value = value.multiply(BigDecimal.valueOf(factor));
            return this;
        }

        public boolean eq(IntegralDataTypeHolder other) {
            checkInitialized();
            return value.compareTo(extractBigDecimal(other)) == 0;
        }

        public boolean eq(long value) {
            checkInitialized();
            return this.value.compareTo(BigDecimal.valueOf(value)) == 0;
        }

        public boolean lt(IntegralDataTypeHolder other) {
            checkInitialized();
            return value.compareTo(extractBigDecimal(other)) < 0;
        }

        public boolean lt(long value) {
            checkInitialized();
            return this.value.compareTo(BigDecimal.valueOf(value)) < 0;
        }

        public boolean gt(IntegralDataTypeHolder other) {
            checkInitialized();
            return value.compareTo(extractBigDecimal(other)) > 0;
        }

        public boolean gt(long value) {
            checkInitialized();
            return this.value.compareTo(BigDecimal.valueOf(value)) > 0;
        }

        public IntegralDataTypeHolder copy() {
            BigDecimalHolder copy = new BigDecimalHolder();
            copy.value = value;
            return copy;
        }

        public Number makeValue() {
            checkInitialized();
            return value;
        }

        public Number makeValueThenIncrement() {
            final Number result = makeValue();
            value = value.add(BigDecimal.ONE);
            return result;
        }

        public Number makeValueThenAdd(long addend) {
            final Number result = makeValue();
            value = value.add(BigDecimal.valueOf(addend));
            return result;
        }

        public String toString() {
            return "BigDecimalHolder[" + value + "]";
        }

        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            BigDecimalHolder that = (BigDecimalHolder) o;

            return this.value == null ? that.value == null : this.value.equals(that.value);
        }

        public int hashCode() {
            return value != null ? value.hashCode() : 0;
        }
    }

    /**
     * Disallow instantiation of IdentifierGeneratorHelper.
     */
    private IdGeneratorHelper() {
    }
}
