package org.simpl4;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;


public class PrintHier {
	private static final String PADDING = "        ";
	private static final String PADDING_WITH_COLUMN = "   |    ";
	private static final String PADDING_WITH_ENTRY = "   |--- ";
	private static final String BASE_CLASS = Object.class.getName();

	private final Map<String, List<String>> subClazzEntries = new HashMap<>();

	public static void main(final String[] args) {
	}

	public PrintHier(final Class<?>... clazzes) {
		// get all entries of tree
		traverseClasses(clazzes);
	}

	public void printHierarchy() {
		// print collected entries as ASCII tree
		printHierarchy(BASE_CLASS, new Stack<Boolean>());
	}

	private void printHierarchy(final String clazzName, final Stack<Boolean> moreClassesInHierarchy) {
		if (!moreClassesInHierarchy.empty()) {
			for (final Boolean hasColumn : moreClassesInHierarchy.subList(0, moreClassesInHierarchy.size() - 1)) {
				System.out.print(hasColumn.booleanValue() ? PADDING_WITH_COLUMN : PADDING);
			}
		}

		if (!moreClassesInHierarchy.empty()) {
			System.out.print(PADDING_WITH_ENTRY);
		}

		System.out.println(clazzName);

		if (subClazzEntries.containsKey(clazzName)) {
			final List<String> list = subClazzEntries.get(clazzName);

			for (int i = 0; i < list.size(); i++) {
				// if there is another class that comes beneath the next class, flag this level
				moreClassesInHierarchy.push(new Boolean(i < list.size() - 1));

				printHierarchy(list.get(i), moreClassesInHierarchy);

				moreClassesInHierarchy.removeElementAt(moreClassesInHierarchy.size() - 1);
			}
		}
	}

	private void traverseClasses(final Class<?>... clazzes) {
		// do the traverseClasses on each provided class (possible since Java 8)
		Arrays.asList(clazzes).forEach(c -> traverseClasses(c, 0));
	}

	private void traverseClasses(final Class<?> clazz, final int level) {
		final Class<?> superClazz = clazz.getSuperclass();

		if (superClazz == null) {
			// we arrived java.lang.Object
			return;
		}

		final String name = clazz.getName();
		final String superName = superClazz.getName();

		if (subClazzEntries.containsKey(superName)) {
			final List<String> list = subClazzEntries.get(superName);

			if (!list.contains(name)) {
				list.add(name);
				Collections.sort(list); // SortedList
			}
		} else {
			subClazzEntries.put(superName, new ArrayList<String>(Arrays.asList(name)));
		}

		traverseClasses(superClazz, level + 1);
	}
}
