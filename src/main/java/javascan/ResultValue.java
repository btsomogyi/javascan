package javascan;

//ENUM to store the result types from probe attempts
public enum ResultValue {
	OPEN(0), CLOSED(1), FILTERED(2), ERROR(3);
	private int value;

	private ResultValue(int value) {
		this.value = value;
	}

	public int getValue() {
		return value;
	}

	public static final int size = ResultValue.values().length;
};