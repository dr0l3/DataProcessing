package RandomTest;

import com.google.gson.TypeAdapter;
import com.google.gson.internal.bind.TypeAdapters;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Remove;

import java.io.IOException;
import java.util.Objects;

/**
 * Created by Rune on 23-04-2016.
 */
public class FilterAdapter extends TypeAdapter<Filter> {

    @Override
    public void write(JsonWriter jsonWriter, Filter filter) throws IOException {
        jsonWriter.beginObject();
        if(filter instanceof Remove){
            jsonWriter.name("cols").value(((Remove) filter).getAttributeIndices());
        }
        jsonWriter.endObject();

    }

    @Override
    public Filter read(JsonReader jsonReader) throws IOException {
        final Remove filter = new Remove();
        jsonReader.beginObject();
        while(jsonReader.hasNext()){
            if(jsonReader.nextName().equals("cols"))
                filter.setAttributeIndices(jsonReader.nextString());
        }
        return filter;
    }
}
