package RandomTest;

import com.google.gson.InstanceCreator;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Remove;

import java.lang.reflect.Type;

/**
 * Created by Rune on 23-04-2016.
 */
public class FilterInstanceCreator implements InstanceCreator<Filter> {
    @Override
    public Filter createInstance(Type type) {
        return new Remove();
    }
}
