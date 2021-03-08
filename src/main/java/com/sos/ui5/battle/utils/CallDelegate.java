package com.sos.ui5.battle.utils;

import javax.persistence.EntityManager;

public interface CallDelegate<E> {

	E call(EntityManager em) throws Exception;

}