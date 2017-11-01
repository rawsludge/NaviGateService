package net.mobilim.NaviGateService.Services;

import com.jcabi.aspects.Loggable;
import net.mobilim.NaviGateData.Entities.*;
import net.mobilim.NaviGateData.Repositories.*;
import net.mobilim.NaviGateService.Helpers.XmlDefinitions;
import net.mobilim.NaviGateService.Managers.DownloadManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.InvalidObjectException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

@Component
@Transactional(rollbackFor = {Exception.class})
public class ProductService {
    private final SimpleDateFormat dateFormatter = new SimpleDateFormat("MMddyyyy");
    private final SimpleDateFormat timeFormater = new SimpleDateFormat("HHmm");
    private final Logger logger = LogManager.getLogger(ProductService.class);

    @Autowired
    private DownloadManager downloadManager;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private DestinationRepository destinationRepository;
    @Autowired
    private PortRepository portRepository;
    @Autowired
    private ShipRepository shipRepository;
    @Autowired
    private CabinLocationRepository cabinLocationRepository;
    @Autowired
    private CabinDeckRepository cabinDeckRepository;
    @Autowired
    private CategoryRepository categoryRepository;
    @Autowired
    private ItineraryRepository itineraryRepository;

    @Loggable(Loggable.INFO)
    public Date saveOrUpdateProduct(JSONObject jsonObject) throws Exception {
        Date date;
        Destination destination = prepareDestination(jsonObject.getJSONObject("Destination"));
        //Port embarkPort = preparePort(jsonObject.getJSONObject("EmbarkPort"));
        //Port debarkPort = preparePort(jsonObject.getJSONObject("DebarkPort"));
        JSONObject tempJsonObject = jsonObject.getJSONObject("Sailing");
        Ship ship = prepareShip(tempJsonObject.getJSONObject("Ship"));
        Integer duration = tempJsonObject.getInt("DurationDays");
        Date sailingDate;
        try {
            sailingDate = dateFormatter.parse(tempJsonObject.get("Date").toString());
            date = sailingDate;
        } catch (ParseException e) {
            logger.error("Error occured while Date field parsing.", e);
            throw e;
        }

        String sailingID = jsonObject.get("SailingId").toString();
        Integer maxOccupancy = jsonObject.getInt("MaxOccupancy");

        Product product = productRepository.findBySailingID(sailingID);
        if (product == null) {
            product = new Product();
            product.setPorts(new ArrayList<Port>());
        }
        product.setDestination(destination);
        //product.setEmbarkPort(embarkPort);
        product.setShip(ship);
        product.setDuration(duration);
        product.setSailingDate(sailingDate);
        product.setMaxOccupancy(maxOccupancy);
        //product.setDebarkPort(debarkPort);
        product.setSailingID(sailingID);
        product.setCruiseLineCode("PCL");
        product.setLastUpdateDate(new Date());

        Itinerary itinerary = prepareItinerary( product, jsonObject);
        product.setItinerary(itinerary);

        startDetailSync(product);
        productRepository.save(product);
        return date;
    }

    @Loggable(Loggable.INFO)
    private Destination prepareDestination(JSONObject jsonObject) {
        String code = jsonObject.getString("Code");
        String name = jsonObject.getString("Name");
        Destination destination = destinationRepository.checkAndSave(code, name);
        return destination;
    }

    /*
    @Loggable(Loggable.DEBUG)
    private Port preparePort(JSONObject jsonObject) {
        String code = jsonObject.getString("Code");
        String name = jsonObject.getString("Name");
        Port port = portRepository.checkAndSave(code, name);
        return port;
    }
    */

    @Loggable(Loggable.INFO)
    private Ship prepareShip(JSONObject jsonObject) {
        String code = jsonObject.getString("Code");
        String name = jsonObject.getString("Name");
        Ship ship = shipRepository.checkAndSave(code, name);
        return ship;
    }

    @Loggable(Loggable.INFO)
    private Itinerary prepareItinerary(Product product, JSONObject jsonObject) {

        String sailingId = jsonObject.get("SailingId").toString();

        JSONObject sailingObject = jsonObject.getJSONObject("Sailing");
        String sailingDate = sailingObject.get("Date").toString();
        Integer duration = sailingObject.getInt("DurationDays");
        String shipCode = sailingObject.getJSONObject("Ship").getString("Code");

        JSONObject itineraryDeatil = null;

        try {

            String xmlPostData = String.format(XmlDefinitions.ITINERARY_DETAIL, sailingId, sailingDate, duration.toString(), shipCode);
            logger.info("Begin itinerary detail download. Begin date:{}", sailingId);
            itineraryDeatil = downloadManager.download(xmlPostData);
            logger.info("End itinerary detail download. request");
        }
        catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }

        jsonObject = jsonObject.getJSONObject("Itinerary");

        String code = jsonObject.getString("Code");
        String name = jsonObject.getString("Description");
        Integer portCount = jsonObject.getInt("PortCnt");

        Itinerary itinerary = itineraryRepository.findByCode(code);
        if (itinerary == null) {
            itinerary = new Itinerary();
        }
        itinerary.setCode(code);
        itinerary.setName(name);
        itinerary.setPortCount(portCount);

        try {

            Object itineraryPortsObject = findObjectByPath(itineraryDeatil, "ItineraryResponse/ItinInformation");
            if (itineraryPortsObject instanceof JSONArray) {
                for (Object item : (JSONArray) itineraryPortsObject) {
                    JSONObject itiItem = (JSONObject)item;
                    if( itiItem.getString("PortCode").isEmpty() ) continue;
                    code = itiItem.getString("PortCode");
                    Object nameObj = itiItem.get("PortName");
                    if( nameObj instanceof JSONObject) {
                        JSONObject nameJson = (JSONObject) nameObj;
                        name = String.format("%s / %s", nameJson.getString("Pier"), nameJson.getString("content") );
                    }
                    else
                        name = itiItem.getString("PortName");
                    String date = itiItem.get("Date").toString();
                    Port port = new Port(code, name);
                    port.setDate(dateFormatter.parse(date));
                    port.setProduct(product);
                    Object portScheduleObj = itiItem.get("PortSchedule");
                    if( portScheduleObj instanceof JSONArray) {
                        for ( Object portObj : (JSONArray) portScheduleObj ){
                            JSONObject portJson = (JSONObject)portObj;
                            prepareScheduleParameters(port, portJson);
                        }
                    }
                    else if( portScheduleObj instanceof JSONObject) {
                        JSONObject portJson = (JSONObject)portScheduleObj;
                        prepareScheduleParameters(port, portJson);
                    }
                    product.getPorts().add(port);
                }
            }
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
        itineraryRepository.save(itinerary);
        return itinerary;
    }

    private void prepareScheduleParameters(Port port, JSONObject portJson) throws ParseException {
        String code;
        code = portJson.getString("Code");
        String time = portJson.get("Time").toString();
        if( code.equals("AR"))
            port.setArrive(timeFormater.parse(time));
        if( code.equals("DP"))
            port.setDepart(timeFormater.parse(time));
        if(code.equals("BO"))
            port.setBoarding(timeFormater.parse(time));
    }

    @Loggable(Loggable.INFO)
    private void startDetailSync(Product product) throws Exception {

        String guestData = "";
        JSONObject jsonObject;

        for (int index = 0; index < product.getMaxOccupancy(); index++) {
            guestData += String.format("<Guest SeqNumber=\"%d\" AgeCode=\"A\"/>", index + 1);
        }
        String sailingDate = dateFormatter.format(product.getSailingDate());
        String xmlPostData = String.format(XmlDefinitions.CATEGORY, product.getSailingID(),
                sailingDate, product.getShip().getCode(), String.format(
                        "<NumberOfGuests>%d</NumberOfGuests>%s", product.getMaxOccupancy(), guestData));

        jsonObject = downloadManager.download(xmlPostData);
        Object object = findObjectByPath(jsonObject, "CategoryAvailabilityResponse/Category/");
        if (object instanceof JSONArray) {
            JSONArray jsonArray = (JSONArray) object;
            //logger.info(jsonArray.toString());
            for (Object item : jsonArray) {
                jsonObject = (JSONObject) item;


                Category category = null;
                if (product.getId() != 0) {
                    category = categoryRepository.findByProduct(jsonObject.get("Code").toString(), product);
                }
                if (category == null)
                    category = new Category();
                category.setCabinStatus(jsonObject.getJSONObject("Status").getString("Code"));
                category.setCabinType(jsonObject.getString("CabinType"));
                category.setCabinSubTypeDesc(jsonObject.getString("CabinSubTypeDescription"));
                category.setCabinSubType(jsonObject.get("CabinSubType").toString());
                category.setCode(jsonObject.get("Code").toString());
                category.setName(jsonObject.get("Name").toString());
                category.setLastUpdateDate(new Date());
                category.setProduct(product);

                String code = jsonObject.getJSONObject("CabinLocation").getString("Code");

                CabinLocation cabinLocation = cabinLocationRepository.findByCode(code);
                if (cabinLocation == null)
                    cabinLocation = new CabinLocation();
                cabinLocation.setCode(code);
                cabinLocation.setName(jsonObject.getJSONObject("CabinLocation").getString("Description"));
                cabinLocation.setLastUpdateDate(new Date());
                cabinLocationRepository.save(cabinLocation);

                category.setCabinLocation(cabinLocation);

                if (category.getGuestTypes().size() == 0) {
                    Object guestTypes = jsonObject.get("GuestType");
                    if (guestTypes instanceof JSONArray) {
                        for (Object guestObject : (JSONArray) guestTypes) {

                            JSONObject guestTypeJSON = (JSONObject) guestObject;
                            GuestType guestType;
                            guestType = new GuestType();
                            guestType.setCategory(category);
                            code = guestTypeJSON.get("Code").toString();
                            guestType.setCode(code);
                            guestType.setDescription(guestTypeJSON.getString("Description"));
                            guestType.setStatusCode(guestTypeJSON.getJSONObject("Status").getString("Code"));
                            guestType.setLastUpdateDate(new Date());

                            JSONObject transportationJSON = guestTypeJSON.getJSONObject("Transportation");
                            Transportation transportation;
                            transportation = new Transportation();
                            transportation.setStatusCode(transportationJSON.getJSONObject("Status").getString("Code"));
                            transportation.setAmount(transportationJSON.getBigDecimal("Amount"));
                            transportation.setDescription(transportationJSON.getString("Description"));
                            transportation.setGuestMin(transportationJSON.getJSONObject("Guests").getInt("Minimum"));
                            transportation.setGuestMax(transportationJSON.getJSONObject("Guests").getInt("Maximum"));
                            transportation.setRateCode(transportationJSON.getJSONObject("Rate").getString("Code"));
                            transportation.setRateName(transportationJSON.getJSONObject("Rate").getString("Name"));
                            transportation.setRateType(transportationJSON.getJSONObject("Rate").getString("Type"));
                            transportation.setRatePortCharge(transportationJSON.getJSONObject("Rate").getString("PortCharge"));
                            transportation.setTaxFreeAmount(transportationJSON.getBigDecimal("TaxFeeAmount"));
                            transportation.setType(transportationJSON.getString("Type"));
                            transportation.setLastUpdateDate(new Date());

                            guestType.setTransportation(transportation);
                            category.getGuestTypes().add(guestType);
                        }
                    }
                }

                Object decks = jsonObject.get("Deck");
                if (decks instanceof JSONArray) {
                    for (Object deck : (JSONArray) decks) {
                        JSONObject jsonDeck = (JSONObject) deck;
                        code = jsonDeck.get("Code").toString();
                        CabinDeck cabinDeck = cabinDeckRepository.findByCode(code);
                        if (cabinDeck == null)
                            cabinDeck = new CabinDeck();
                        cabinDeck.setCode(code);
                        cabinDeck.setName(jsonDeck.get("Name").toString());
                        cabinDeck.setLastUpdateDate(new Date());
                        cabinDeckRepository.save(cabinDeck);

                        CategoryCabinDeck categoryCabinDeck = new CategoryCabinDeck();
                        categoryCabinDeck.setCabinDeck(cabinDeck);
                        categoryCabinDeck.setCategory(category);
                        category.getCategoryCabinDecks().add(categoryCabinDeck);
                    }
                }
                product.getCategories().add(category);
            }
        }
    }

    @Loggable(Loggable.INFO)
    private Object findObjectByPath(JSONObject jsonObject, String path) throws Exception {
        String[] objectNames = path.split("/");
        Object returnObject = jsonObject;
        for (String name : objectNames) {
            if (name.isEmpty()) continue;
            if (returnObject instanceof JSONObject) {
                jsonObject = (JSONObject) returnObject;
                if (jsonObject.has(name))
                    returnObject = jsonObject.get(name);
                else
                    throw new InvalidObjectException(name);
            }
        }
        return returnObject;
    }
}
