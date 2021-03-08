package com.sos.ui5.battle.utils;

/**
 * Toutes les exceptions prévues par l'application doivent être une instance de
 * AppException
 */
public class AppException extends RuntimeException {
	/**
	 * 
	 */
	private static final long serialVersionUID = 4801740090790280742L;
	
  public AppException() {
      super();
  }

  public AppException(String message) {
      super(message);
  }

  public AppException(String message, Throwable cause) {
      super(message, cause);
  }

  public AppException(Throwable cause) {
      super(cause);
  }

}
