package fr.enseeiht.ocl.xtext.utils;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Set;

public class ConstructorInstanciator {

	/**
	 * Renvoie une instance obtenue par l'appel d'un constructeur sans paramètre d'une classe donnée.
	 * Si la classe n'a pas de constructeur public sans paramètre (cas des listes EMF issues de la
	 * navigation d'un modèle, ex. EObjectContainmentEList), on retombe sur une collection standard
	 * du même genre (Set -> LinkedHashSet, sinon ArrayList), afin que les itérateurs (select, reject,
	 * collect, ...) puissent construire leur résultat.
	 * @param source classe à instancier
	 * @return instance
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 * @throws InvocationTargetException
	 * @throws SecurityException
	 * @throws NoSuchMethodException
	 */
	public static Object instantiateParameterlessConstructor(Class<?> source) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
		try {
			// Récupère un constructeur sans paramètres, puis instancie
			Constructor<?> parameterlessConstructor = source.getConstructor();
			return parameterlessConstructor.newInstance();
		} catch (NoSuchMethodException e) {
			// Pas de constructeur sans argument : on retombe sur une collection standard.
			if (Set.class.isAssignableFrom(source)) {
				return new LinkedHashSet<>();
			}
			return new ArrayList<>();
		}
	}
}