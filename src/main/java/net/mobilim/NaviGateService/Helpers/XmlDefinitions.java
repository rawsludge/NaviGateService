package net.mobilim.NaviGateService.Helpers;

public class XmlDefinitions {
    public static final String PRODUCT = "" +
            "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
                    "<CruiseLineRequest>\n" +
                    "  <MessageHeader SegmentId=\"MSGHDR\">\n" +
                    "    <MessageId>CCPROAV1</MessageId>\n" +
                    "    <SessionId>1234</SessionId>\n" +
                    "    <CruiseLineCode>PCL</CruiseLineCode>\n" +
                    "    <UserId>optional</UserId>\n" +
                    "    <UserName>username</UserName>\n" +
                    "    <SystemId>E4</SystemId>\n" +
                    "    <AgencyId>1319</AgencyId>\n" +
                    "    <UICode>3</UICode>\n" +
                    "    <VersionNum>3.0</VersionNum>\n" +
                    "    <AttemptCnt>1</AttemptCnt>\n" +
                    "    <SendDescriptionInd>Y</SendDescriptionInd>\n" +
                    "    <Copyright>Copyright (C) 2000 Carnival Corporation.  All rights reserved.</Copyright>\n" +
                    "  </MessageHeader>\n" +
                    "  <ProductAvailabilityRequest SegmentId=\"CCPROAV1\">\n" +
                    "    <DepDate From=\"%s\" To=\"%s\" />\n" +
                    "    <DurationDays Minimum=\"%s\" Maximum=\"%s\" />\n" +
                    "    <Ship Code=\"%s\"/>" +
                    "    <SailingStatus Code=\"AV\" />\n" +
                    "    <Destination Code=\"%s\" />\n" +
                    "  </ProductAvailabilityRequest>\n" +
                    "</CruiseLineRequest>";
    public static final String CATEGORY = "" +
            "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
                    "<CruiseLineRequest>\n" +
                    "  <MessageHeader SegmentId=\"MSGHDR\">\n" +
                    "    <MessageId>CCCATAV1</MessageId>\n" +
                    "    <SessionId>1234</SessionId>\n" +
                    "    <CruiseLineCode>PCL</CruiseLineCode>\n" +
                    "    <UserId>optional</UserId>\n" +
                    "    <UserName>username</UserName>\n" +
                    "    <SystemId>E4</SystemId>\n" +
                    "    <AgencyId>1319</AgencyId>\n" +
                    "    <UICode>3</UICode>\n" +
                    "    <VersionNum>3.0</VersionNum>\n" +
                    "    <AttemptCnt>1</AttemptCnt>\n" +
                    "    <SendDescriptionInd>Y</SendDescriptionInd>\n" +
                    "    <Copyright>Copyright (C) 2000 Carnival Corporation.  All rights reserved.</Copyright>\n" +
                    "  </MessageHeader>\n" +
                    "  <CategoryAvailabilityRequest SegmentId=\"CCCATAV1\">\n" +
                    "    <SailingId>%s</SailingId>\n" +
                    "    <SailDate>%s</SailDate>\n" +
                    "    <Ship Code=\"%s\" />\n" +
/*                    "    <City Code=\"%s\" />\n" +
                    "    <Currency Code=\"EUR\" />\n" +*/
                    "    <Transportation Type=\"O\" />\n" +
                    "    <Rate Code=\"BNN\" />\n" +
                    "    <NumberOfGuests>2</NumberOfGuests>\n" +
                    "    <VoyageLimits>Y</VoyageLimits>\n" +
                    "    <Guest SeqNumber=\"1\" AgeCode=\"A\"/>\n" +
                    "    <Guest SeqNumber=\"2\" AgeCode=\"A\"/>\n" +
                    "    <IncludeGroupsInd>Y</IncludeGroupsInd>\n" +
                    "  </CategoryAvailabilityRequest>\n" +
                    "</CruiseLineRequest>";

}
