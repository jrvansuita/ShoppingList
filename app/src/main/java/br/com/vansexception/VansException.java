package br.com.vansexception;

public class VansException extends Exception{
	

	private static final long serialVersionUID = 1L;

	public VansException(String msg) {
		super(msg);
	}

	public VansException(String msg, Throwable cause) {
		super(msg,cause);
	}
}
