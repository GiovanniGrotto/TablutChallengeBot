package it.unibo.ai.didattica.competition.tablut.customizations;
import it.unibo.ai.didattica.competition.tablut.domain.Action;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.unibo.ai.didattica.competition.tablut.domain.State;


import java.io.IOException;
import java.util.*;
import java.io.*;

public class LimitedHashMap<K, V> extends LinkedHashMap<K, V> {
    private int maxSize = 1000000;

    public LimitedHashMap(int maxSize) {
        this.maxSize = maxSize;
    }
    public LimitedHashMap() {
        super();
    }

    public LimitedHashMap(int maxSize, String jsonFile, String mapType) throws IOException {
        long start = System.currentTimeMillis();
        this.maxSize = maxSize;
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            File file = new File(jsonFile);
            if (file.exists()) {
                LimitedHashMap<K, V> loadedMap = objectMapper.readValue(file, LimitedHashMap.class);
                if(Objects.equals(mapType, "stateAction")){
                    LimitedHashMap<String, List<Action>> convertedMap = convertToStateAction(loadedMap);
                    this.putAll((Map<? extends K, ? extends V>) convertedMap);
                }else if(Objects.equals(mapType, "stateEvaluation")) this.putAll(loadedMap);
                else System.out.println("TIPO DI HASHMAP NON PRESENTE");
            }
            System.out.println("Time needed to load "+this.size()+" elements: "+(System.currentTimeMillis()-start));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
        return size() > maxSize;
    }

    public void writeToJsonFile(String jsonFile) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.writeValue(new File(jsonFile), this);
    }

    public LimitedHashMap<String, List<Action>> convertToStateAction(LimitedHashMap<K, V> loadedMap) throws IOException {
        LimitedHashMap<String, List<Action>> convertedMap = new LimitedHashMap<>();
        for (Map.Entry<K, V> entry : loadedMap.entrySet()) {
            String key = (String) entry.getKey();
            List<LinkedHashMap<String, String>> value = (List<LinkedHashMap<String, String>>) entry.getValue();
            List<Action> actionList = new ArrayList<>();
            for (LinkedHashMap<String, String> linkedHashMap : value) {
                String turn = linkedHashMap.get("turn");
                if (Objects.equals(turn, "WHITE")) {
                    actionList.add(new Action(linkedHashMap.get("from"), linkedHashMap.get("to"), State.Turn.WHITE));
                } else {
                    actionList.add(new Action(linkedHashMap.get("from"), linkedHashMap.get("to"), State.Turn.BLACK));
                }
            }
            convertedMap.put(key, actionList);
        }
        return convertedMap;
    }
}
