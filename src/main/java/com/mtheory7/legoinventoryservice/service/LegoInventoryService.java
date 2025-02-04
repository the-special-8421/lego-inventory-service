package com.mtheory7.legoinventoryservice.service;

import com.mtheory7.legoinventoryservice.api.RebrickableApi;
import com.mtheory7.legoinventoryservice.entities.RebrickableResultDTO;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class LegoInventoryService {
    // Services
    private final RebrickableApi rebrickableApi;
    @Value("${REBRICKABLE_API_KEY:}")
    private String rebrickableApiKey;
    // Setup
    private final List<String> FILE_HEADER = Arrays.asList("Set Number", "Name", "Year", "Theme ID", "Parts", "Status", "Image URL", "Set URL");
    private final String FILE_PATH = "src/main/resources/LegoInventory.csv";
    // Data
    private final List<String> SETS_USED = Arrays.asList("75345","75317","76831","75271","60420","10355","10333","75395","7131","40640","30280","30584","75363","76263","76289","42165","76191","10497","75355","75279","6495154","40746","30620","30680","75295","76263","40721","31113","10369","21328","75389","75193","40743","30460","30670","76275","75368","40696","31113","10368","40649","60440","7110","30340","30570","60399","42147","40646","31113","42178","42179","10339","7111","70822","30663","75344","31134","40462","75388","75356","75379","7131","70823","30665","40642","75332","40463","31136","75383","40719","7121","70824","30685","42163","75332","31136","76278","40719","7106","30701","40468","42147","31136","75375","42161","6556842","30568","75391","31145","76915","10281","75398","6594","30653","60458","31145","75360","40613","75394","30651","40644","31145","76424","75380","21336","71803","60458","60428","60404","75381","10341","30645","75373","60430","71426","10300","30652","31134","75385","40648","30654","40748","40585","75300","30677","31134","31164","75304","30682","76241","31164","75376","2045","40619","31164","42201","40357","40619","40220","40574","30701","40631","31088","21342","30710","40631","31088","31161","30688","40715","31088","31134","40290","40712","40687","40517","76958","75317","75880","60392","75886","60318","76908","75370","75878","76307","76918","40547","75887","40547","75892","40728","76918","31159","75890","31159","10355","31159","31134","31134","40570","40524","40461","40650","40712","60404","71487");
    private final List<String> SETS_SEALED = Arrays.asList("912411","75375","6495154","30654","40630","40715","30390","76275","40644","30645","40669","70841","30685","40671","6386182","30640","42148","70823","30701","40460","40712","30682","31140","40693","40277","40641","5002812","30683","40641","40306","30670","40638","42163","30645","40639","5006085","30683","60376","5006746","30670","60400","31140","30657","76275","42163","30701","71487","40581","30670","71816","40726","30685","42197","40688","30677","40756","40687","30651","75369","40719","30663","40615","40574","30710","75378","60430","30680","31163","40649","30433","11037","40585","30652","40686","40689","30582","75333","40357","30710","70825","10355","911724","21326","952310","76908","122333","76907","912175","76934","911943","76425","5002045","76307","5002203","40688","5002948","40604","912310","60438","30701","60384","30701","60283","30280","31157","30280","76944","30528","76303","30340","76421","30460","40699","40564","76293","70816","31212");
    private final Set<String> UNIQUE_SETS = new HashSet<>();

    private final Map<String, RebrickableResultDTO> rebrickableResultsMap = new HashMap<>();

    public LegoInventoryService(RebrickableApi rebrickableApi) {
        this.rebrickableApi = rebrickableApi;
        Collections.sort(SETS_USED);
        Collections.sort(SETS_SEALED);
        UNIQUE_SETS.addAll(SETS_USED);
        UNIQUE_SETS.addAll(SETS_SEALED);
    }

    public String refreshInventory() throws InterruptedException {
        if (rebrickableApiKey == null || rebrickableApiKey.isEmpty()) return "Set environment variable REBRICKABLE_API_KEY to a valid Rebrickable API Key.";
        String response = "<a>Started job at " + LocalDateTime.now();
        for (String setNumber : UNIQUE_SETS) {
            Thread.sleep(1300);
            RebrickableResultDTO rebrickableResponseDTO = rebrickableApi.findSetBySetNumber(rebrickableApiKey, setNumber+"-1");
            rebrickableResultsMap.put(setNumber+"-1", rebrickableResponseDTO);
        }
        return response + "<br>Finished at " + LocalDateTime.now() + "</a>" ;
    }

    public String getDisplayString() {
        StringBuilder response = new StringBuilder("<p>Lego Inventory<br>");
        int setIndex = 1;
        for (Map.Entry<String, RebrickableResultDTO> entry : rebrickableResultsMap.entrySet()) {
            response.append(setIndex)
                    .append(" - Set: ").append(entry.getValue().getSet_num())
                    .append(" - Name: ").append(entry.getValue().getName())
                    .append(" - Parts: ").append(entry.getValue().getNum_parts())
                    .append("<br>");
            setIndex++;
        }
        return response + "</p>";
    }

    public String generateFile() {
        String response = "<a>Started generating file at " + LocalDateTime.now();
        writeFile();
        return response + "<br>Finished at " + LocalDateTime.now() + "</a>" ;
    }

    private void writeFile() {
        try {
            FileWriter fileWriter = new FileWriter(FILE_PATH);
            CSVPrinter csvPrinter = new CSVPrinter(fileWriter, CSVFormat.DEFAULT);
            csvPrinter.printRecord(FILE_HEADER);
            List<List<String>> data = generateData();
            data.sort(Comparator.comparing(l -> l.get(0)));
            for (List<String> row : data) {
                csvPrinter.printRecord(row);
            }
            fileWriter.flush();
            fileWriter.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private List<List<String>> generateData() {
        List<List<String>> data = new ArrayList<>();
        List<String> setsToSkip = new ArrayList<>();
        for (String setNumber : SETS_USED) {
            if (setsToSkip.contains(setNumber)) continue;
            List<String> row = new ArrayList<>();
            String setNumberDashOne = setNumber+"-1";
            if (rebrickableResultsMap.containsKey(setNumberDashOne)) {
                row.add(rebrickableResultsMap.get(setNumberDashOne).getSet_num());
                row.add(rebrickableResultsMap.get(setNumberDashOne).getName());
                row.add(rebrickableResultsMap.get(setNumberDashOne).getYear());
                row.add(rebrickableResultsMap.get(setNumberDashOne).getTheme_id().toString());
                row.add(rebrickableResultsMap.get(setNumberDashOne).getNum_parts().toString());
                row.add("SETS_USED");
                row.add(rebrickableResultsMap.get(setNumberDashOne).getSet_img_url());
                row.add(rebrickableResultsMap.get(setNumberDashOne).getSet_url());
            } else {
                row.add(setNumberDashOne);
                row.add("No information loaded yet");
            }
            setsToSkip.add(setNumber);
            data.add(row);
        }
        for (String setNumber : SETS_SEALED) {
            List<String> row = new ArrayList<>();
            String setNumberDashOne = setNumber+"-1";
            if (rebrickableResultsMap.containsKey(setNumberDashOne)) {
                row.add(rebrickableResultsMap.get(setNumberDashOne).getSet_num());
                row.add(rebrickableResultsMap.get(setNumberDashOne).getName());
                row.add(rebrickableResultsMap.get(setNumberDashOne).getYear());
                row.add(rebrickableResultsMap.get(setNumberDashOne).getTheme_id().toString());
                row.add(rebrickableResultsMap.get(setNumberDashOne).getNum_parts().toString());
                row.add("SETS_SEALED");
                row.add(rebrickableResultsMap.get(setNumberDashOne).getSet_img_url());
                row.add(rebrickableResultsMap.get(setNumberDashOne).getSet_url());
            } else {
                row.add(setNumberDashOne);
                row.add("No information loaded yet");
            }
            data.add(row);
        }
        return data;
    }
}
