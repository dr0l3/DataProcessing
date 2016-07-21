package RandomTest;

import ArffFile.CompleteFeatureFileGenerator;
import Core.BiasConfiguration;
import Core.ClassifierType;
import weka.classifiers.Classifier;
import weka.classifiers.meta.FilteredClassifier;
import weka.core.Instance;
import weka.core.Instances;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Remove;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * Created by Rune on 23-04-2016.
 */
public class SerializeClassifierToJsonTEst {
    private static FilteredClassifier eventClassifier;

    public static void main(String[] args) {
        try {
            eventClassifier = (FilteredClassifier) weka.core.SerializationHelper.read("D:\\Projekter\\MoshiJSonAdapterTesting\\assets\\1461324051401_filteredclassifierevent_sniffer.model");
            Filter copiedFilter = null;
            System.out.println(eventClassifier.getFilter().getClass().getSimpleName());
            if(eventClassifier.getFilter() instanceof Remove){
                Remove temp = (Remove) eventClassifier.getFilter();
                System.out.println(temp.getAttributeIndices());
                Remove newFilter = new Remove();
                String[] options = new String[2];
                options[0] = "-R";
                options[1] = temp.getAttributeIndices();
                newFilter.setOptions(options);
                System.out.println((Arrays.toString(options)));
                copiedFilter = newFilter;
            }
            ClassifierType type_of_classifer = ClassifierType.EVENT_SNIFFER;
            System.out.println("Importing data");
            String fromLocation = "D:\\Dropbox\\Thesis\\Data\\RawDataProx";
            //String fromLocation = "D:\\Dropbox\\Thesis\\Data\\RawData";
            String toLocation = "D:\\Dropbox\\Thesis\\Data\\CompleteFeatureFiles\\";
            double window_size_seconds = 3;

            BiasConfiguration biasForIT119 = new BiasConfiguration(
                    0.005f, -0.030f, -0.089f,
                    -0.011f, 0.024f, 0.083f);
            BiasConfiguration megaBias = new BiasConfiguration(100, 100, 100, 100, 100, 100);
            BiasConfiguration nullBias = new BiasConfiguration(0, 0 , 0, 0, 0, 0);

            List<String> featureFileURIs = CompleteFeatureFileGenerator.createCompleteFeatureFileWithProximitySeparateFile(
                    fromLocation,toLocation, window_size_seconds, type_of_classifer, biasForIT119);
            //load in all the different data sets and set weights appropriately
            List<Instances> all_data_inp = new ArrayList<>();
            for (String file : featureFileURIs) {
                Instances data = CompleteFeatureFileGenerator.getInstances(file);
            /*for (Instance instance : data) {
                if(file.contains("hand") && instance.classValue() == 0 && instance.value(instance.numAttributes()-2)> 2){
                    instance.setWeight(1.0);
                } else {
                    instance.setWeight(0.2);
                }
            }*/
                all_data_inp.add(data);
            }

            Instances alldata = new Instances(all_data_inp.get(0));
            for (int i = 1; i < all_data_inp.size(); i++) {
                for (Instance instance : all_data_inp.get(i)) {
                    alldata.add(instance);
                }
            }

            Random random = new Random();
            List<Integer> indexes = new ArrayList<>();
            for (int i = 0; i < 10 ; i++) {
                int next = random.nextInt(alldata.size()-1);
                indexes.add(next);
            }

            for (Integer index : indexes) {
                System.out.println(eventClassifier.classifyInstance(alldata.get(index)));
            }

            System.out.println();

            eventClassifier.setFilter(copiedFilter);
            eventClassifier.getFilter().setInputFormat(alldata);

            for (Integer index : indexes) {

                System.out.println(eventClassifier.classifyInstance(alldata.get(index)));
            }



        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
