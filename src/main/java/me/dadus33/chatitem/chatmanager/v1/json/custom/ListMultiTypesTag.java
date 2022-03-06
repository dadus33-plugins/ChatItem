package me.dadus33.chatitem.chatmanager.v1.json.custom;

import java.lang.reflect.Field;
import java.util.List;

import com.github.steveice10.opennbt.tag.builtin.ListTag;
import com.github.steveice10.opennbt.tag.builtin.Tag;

public class ListMultiTypesTag extends ListTag {
	
    /**
     * Creates an empty list tag with the specified name and no defined type.
     *
     * @param name The name of the tag.
     */
    public ListMultiTypesTag(String name) {
        super(name);
    }

    /**
     * Creates an empty list tag with the specified name and type.
     *
     * @param name The name of the tag.
     * @param type Tag type of the list.
     */
    public ListMultiTypesTag(String name, Class<? extends Tag> type) {
        super(name, type);
    }

    /**
     * Creates a list tag with the specified name and value.
     * The list tag's type will be set to that of the first tag being added, or null if the given list is empty.
     *
     * @param name  The name of the tag.
     * @param value The value of the tag.
     * @throws IllegalArgumentException If all tags in the list are not of the same type.
     */
    public ListMultiTypesTag(String name, List<Tag> value) throws IllegalArgumentException {
        super(name, value);
    }

    /**
     * Adds a tag to this list tag.
     * If the list does not yet have a type, it will be set to the type of the tag being added.
     *
     * @param tag Tag to add. Should not be null.
     * @return If the list was changed as a result.
     * @throws IllegalArgumentException If the tag's type differs from the list tag's type.
     */
    @SuppressWarnings("unchecked")
	public boolean add(Tag tag) throws IllegalArgumentException {
        if(tag == null) {
            return false;
        }
        try {
        	// TODO don't use reflection
        	Field valField = ListTag.class.getDeclaredField("value");
        	valField.setAccessible(true);
	        ((List<Tag>) valField.get(this)).add(tag);
	        return true;
        } catch (Exception e) {
        	e.printStackTrace();
        	return false;
		}
    }
}