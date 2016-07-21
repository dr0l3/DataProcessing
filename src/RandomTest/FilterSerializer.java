package RandomTest;

import com.google.gson.JsonElement;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import weka.filters.Filter;

import java.lang.reflect.Type;

/**
 * Created by Rune on 23-04-2016.
 */
public class FilterSerializer implements JsonSerializer<Filter> {
    @Override
    public JsonElement serialize(Filter filter, Type type, JsonSerializationContext jsonSerializationContext) {
        return null;
    }
}
