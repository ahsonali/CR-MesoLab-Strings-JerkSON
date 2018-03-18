package io.zipcoder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Map;

public class ItemParser
{
    private Pattern pattern;
    private Matcher matcher;
    private Integer countExceptions = 0;
    private Main main = new Main();
    private Map<String, ArrayList<Item>> groceryMap = new HashMap<String, ArrayList<Item>>();
    private String setTimes;

    //
    private ArrayList<String> splitStringWithRegexPattern(String stringPattern, String inputString)
    {
        return new ArrayList<String>(Arrays.asList(inputString.split(stringPattern)));
    }

    //This method calls the above method
    public ArrayList<String> parseRawDataIntoStringArray(String rawData){
        String stringPattern = "##";
        ArrayList<String> response = splitStringWithRegexPattern(stringPattern , rawData);
        return response;
    }

    //not used really, but tested
    public ArrayList<String> findKeyValuePairsInRawItemData(String rawItem)
    {
        String stringPattern = "[@|^|*|%|!|;]";
        ArrayList<String> response = splitStringWithRegexPattern(stringPattern , rawItem);
        return response;
    }


    //Uses the helper methods created below named checkName, checkPrice, checkType, checkExpiration
    public Item parseStringIntoItem(String rawItem) throws ItemParseException
    {

        if (checkName(rawItem) == null || checkPrice(rawItem) == null) {
            throw new ItemParseException();
        }


        String name = checkName(rawItem);
        Double price = Double.parseDouble(checkPrice(rawItem));
        String type = checkType(rawItem);
        String expiration = checkExpiration(rawItem);


        return new Item(name, price, type, expiration);

    }




    public String checkName(String rawItem)
    {

        String search = "(?<=([Nn][Aa][Mm][Ee][^A-Za-z])).*?(?=[^A-Za-z0])";
        pattern = Pattern.compile(search);
        matcher = pattern.matcher(rawItem);

        if (matcher.find())
        {
            if(!matcher.group().equals(""))
            {
                String name = matcher.group().replaceAll("\\d", "o");
                return name.toLowerCase();
            }
        }

        return null;



   }

    public String checkPrice(String rawItem)
    {

        pattern = Pattern.compile("(?<=([Pp][Rr][Ii][Cc][Ee][^A-Za-z])).*?(?=[^0-9.])");
        matcher = pattern.matcher(rawItem);

        if (matcher.find()) {
            if (!matcher.group().equals("")) {
                return matcher.group();
            }
        }
        return null;

    }

    public String checkType(String rawItem) //throws itemParseException
    {


        pattern = Pattern.compile("(?<=([Tt][Yy][Pp][Ee][^A-Za-z])).*?(?=[^A-Za-z0])");
        matcher = pattern.matcher(rawItem);

        if (matcher.find()) {
            return matcher.group().toLowerCase();
        }
        return null;
    }

    public String checkExpiration(String rawItem)
    {
        pattern = Pattern.compile("(?<=([Ee][Xx][Pp][Ii][Rr][Aa][Tt][Ii][Oo][Nn][^A-Za-z]))(.)+[^#]");
        matcher = pattern.matcher(rawItem);
        if (matcher.find()) {
            return matcher.group();
        }
        return null;
    }

    public Map<String, ArrayList<Item>> getMap()  throws Exception
    {
        ArrayList<String> allItems = parseRawDataIntoStringArray(main.readRawDataToString());

        for (String eachItem : allItems)
        {
            try {
                Item newItem = parseStringIntoItem(eachItem);
                if (!groceryMap.containsKey(newItem.getName()))
                {
                    ArrayList<Item> thisItem = new ArrayList<Item>();
                    thisItem.add(newItem);
                    groceryMap.put(newItem.getName(), thisItem);
                }
                else
                    {
                        groceryMap.get(newItem.getName()).add(newItem);
                    }
            }

            catch (ItemParseException e)
            {
                countExceptions++;
            }
        }

        return groceryMap;
    }

    public String displayOutput() throws Exception{
        groceryMap = getMap();

        StringBuilder sb = new StringBuilder();

        for  (Map.Entry<String, ArrayList<Item>> item: groceryMap.entrySet())
        {
            String upperCase = item.getKey().substring(0, 1).toUpperCase()+ item.getKey().substring(1);

            sb.append("\n" +
                    String.format("%-5s%10s%15s%2d%5s", "name:", upperCase
                            , "seen: ", item.getValue().size(), "  times"));

            sb.append("\n" + String.format("%15s%3s%5s", "===============", "\t\t\t", "===============") + "\n");

            ArrayList<Double> uniquePriceList = getUniquePrices(item);

            for (int i = 0; i <uniquePriceList.size(); i++)
            {
                if(priceOccurencesSeen(item.getValue(), uniquePriceList.get(i)) == 1){
                    setTimes = "  time";
                } else setTimes = "  times";

                sb.append(String.format("%-11s%.2f%15s%2d%5s", "Price:", uniquePriceList.get(i)
                        , "seen: ", priceOccurencesSeen(item.getValue(), uniquePriceList.get(i))
                        , setTimes));
                sb.append("\n" +
                        String.format("%15s%3s%5s", "---------------", "\t\t\t", "---------------") + "\n");
            }

        }

        sb.append("\n"+ String.format("%-20s%10s%2d%5s", "Errors", "seen: ", countExceptions, "  times"));

        return sb.toString();
    }

    public int priceOccurencesSeen(ArrayList<Item>list, Double price)
    {
        int priceCounter = 0;

        for (int i = 0; i < list.size(); i++)
        {
            if(list.get(i).getPrice().equals(price))
            {
                priceCounter++;
            }
        }

        return priceCounter;
    }

    public ArrayList<Double> getUniquePrices(Map.Entry<String,ArrayList<Item>> item)
    {
        ArrayList<Double> uniquePrices = new ArrayList<Double>();

        for (int i = 0; i < item.getValue().size(); i++)
        {
            if(!uniquePrices.contains(item.getValue().get(i).getPrice()))
            {
                uniquePrices.add(item.getValue().get(i).getPrice());
            }
        }

        return uniquePrices;
    }


}
