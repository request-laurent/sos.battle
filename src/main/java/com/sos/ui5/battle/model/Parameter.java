package com.sos.ui5.battle.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;

import org.hibernate.annotations.QueryHints;

import com.sos.ui5.battle.utils.AppException;
import com.sos.ui5.battle.utils.EMUtils;

/**
 * Table de paramètres de l'application web Le code est le clé du parametre Le
 * commentaire décrit à quoi sert le parametre
 */
@Cacheable
@Entity
@Table(name="parameter")
public class Parameter {
	public Parameter() {
		super();
	}

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	// La clé du paramètre
	@Enumerated(EnumType.STRING)
	@Column(unique=true)
	private ParameterKey code;

	// La valeur du paramètre
	@Column(length = 1000)
	private String value;

	// La description de ce que le paramètre fait
	private String description;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public ParameterKey getCode() {
		return code;
	}

	public void setCode(ParameterKey code) {
		this.code = code;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public static Map<ParameterKey, String> getMaps() throws Exception {
		Map<ParameterKey, String> map = new HashMap<>();

		return EMUtils.call(em -> {
			List<Parameter> parameters = em.createQuery("select o from Parameter o", Parameter.class).getResultList();
			for (Parameter param : parameters) {
				map.put(param.getCode(), param.getValue());
			}

			// Ajouter les paramétres non trouvé
			for (ParameterKey key : ParameterKey.values()) {
				if (!map.containsKey(key)) {
					Parameter parameter = new Parameter();
					parameter.setCode(key);
					parameter.setDescription("### TODO ###");
					parameter.setValue("### TODO ###");
					map.put(key, parameter.getValue());
					em.persist(parameter);
				}
			}
			return map;
		});
	}
	
  @PreUpdate
  @PrePersist
  public void check() {
  	if (getValue()==null || getValue().length()==0) {
  		throw new AppException("Ne doit pas être vide");
  	}
  }

	public static String get(ParameterKey code) throws Exception {
		return EMUtils.call(em -> {
			Parameter parameter = em.createQuery("select o from Parameter o where o.code=:code", Parameter.class).setParameter("code", code).setHint(QueryHints.CACHEABLE, true).getSingleResult();
			return parameter.getValue();
		});
	}
	
	public static void set(ParameterKey code, String value) throws Exception {
		EMUtils.call(em -> {
			Parameter parameter = em.createQuery("select o from Parameter o where o.code=:code", Parameter.class).setParameter("code", code).getSingleResult();
			parameter.setValue(value);
			return null;
		});
	}
}