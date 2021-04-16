////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//
// © 2011-2021 Telenav, Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

package com.telenav.mesakit.map.road.name.parser.locales.english;

import com.telenav.mesakit.map.road.name.parser.tokenizer.Token;
import com.telenav.mesakit.map.road.name.parser.tokenizer.Tokenizer;
import com.telenav.mesakit.map.road.name.parser.tokenizer.symbols.Symbol;
import com.telenav.mesakit.map.road.name.parser.tokenizer.symbols.SymbolStream;

import java.util.HashSet;
import java.util.Set;

@SuppressWarnings({ "SpellCheckingInspection" })
public class EnglishTokenizer extends Tokenizer
{
    private final Set<Token> onesDigits = new HashSet<>();

    private final Set<Token> tensDigits = new HashSet<>();

    private final Set<Token> teensDigits = new HashSet<>();

    private final Set<Token> ordinals = new HashSet<>();

    private final Set<Token> numericOrdinalSuffixes = new HashSet<>();

    private final Set<Token> quadrants = new HashSet<>();

    // Syntax

    private final Set<Token> cardinalDirections = new HashSet<>();

    private final Set<Token> roadTypes = new HashSet<>();

    public final Token NORTHWEST = create("Northwest")
            .matchesAnyOf("NORTHWEST", "NW")
            .matchesSequence("NORTH", " ", "WEST")
            .matchesSequence("N", " ", "WEST")
            .addTo(quadrants);

    public final Token NORTHEAST = create("Northeast")
            .matchesAnyOf("NORTHEAST", "NE")
            .matchesSequence("NORTH", " ", "EAST")
            .matchesSequence("N", " ", "EAST")
            .addTo(quadrants);

    public final Token SOUTHWEST = create("Southwest")
            .matchesAnyOf("SOUTHWEST", "SW")
            .matchesSequence("SOUTH", " ", "WEST")
            .matchesSequence("S", " ", "WEST")
            .addTo(quadrants);

    public final Token SOUTHEAST = create("Southeast")
            .matchesAnyOf("SOUTHEAST", "SE")
            .matchesSequence("SOUTH", " ", "EAST")
            .matchesSequence("S", " ", "EAST")
            .addTo(quadrants);

    public final Token NORTH = create("North")
            .matchesAnyOf("NORTH", "NORTHBOUND", "NB", "N")
            .matchesSequence("(", "NORTH", ")")
            .addTo(cardinalDirections);

    public final Token SOUTH = create("South")
            .matchesAnyOf("SOUTH", "SOUTHBOUND", "SB", "S")
            .matchesSequence("(", "SOUTH", ")")
            .addTo(cardinalDirections);

    public final Token EAST = create("East")
            .matchesAnyOf("EAST", "EASTBOUND", "EB", "E")
            .matchesSequence("(", "EAST", ")")
            .addTo(cardinalDirections);

    public final Token WEST = create("West")
            .matchesAnyOf("WEST", "WESTBOUND", "WB", "W")
            .matchesSequence("(", "WEST", ")")
            .addTo(cardinalDirections);

    // Directions

    // NOTE: The tokens in here are above the definitions for the tokens NORTH, SOUTH, EAST and WEST
    // so they will get priority in evaluation (otherwise, the input "NORTH WEST" would always
    // result in the token NORTH and never NORTHWEST)

    public final Token DIGIT = create("Digit").matchesAnyOf("0", "1", "2", "3", "4", "5", "6", "7", "8", "9");

    public final Token ZERO = create("Zero").matchesAnyOf("ZERO").addTo(onesDigits);

    public final Token ONE = create("One").matchesAnyOf("ONE").addTo(onesDigits);

    public final Token TWO = create("Two").matchesAnyOf("TWO").addTo(onesDigits);

    public final Token THREE = create("Three").matchesAnyOf("THREE").addTo(onesDigits);

    public final Token FOUR = create("Four").matchesAnyOf("FOUR").addTo(onesDigits);

    public final Token FIVE = create("Five").matchesAnyOf("FIVE").addTo(onesDigits);

    public final Token SIX = create("Six").matchesAnyOf("SIX").addTo(onesDigits);

    // Numbers

    public final Token SEVEN = create("Seven").matchesAnyOf("SEVEN").addTo(onesDigits);

    public final Token EIGHT = create("Eight").matchesAnyOf("EIGHT").addTo(onesDigits);

    public final Token NINE = create("Nine").matchesAnyOf("NINE").addTo(onesDigits);

    public final Token TEN = create("Ten").matchesAnyOf("TEN").addTo(teensDigits);

    public final Token ELEVEN = create("Eleven").matchesAnyOf("ELEVEN").addTo(teensDigits);

    public final Token TWELVE = create("Twelve").matchesAnyOf("TWELVE").addTo(teensDigits);

    public final Token THIRTEEN = create("Thirteen").matchesAnyOf("THIRTEEN").addTo(teensDigits);

    public final Token FOURTEEN = create("Fourteen").matchesAnyOf("FOURTEEN").addTo(teensDigits);

    public final Token FIFTEEN = create("Fifteen").matchesAnyOf("FIFTEEN").addTo(teensDigits);

    public final Token SIXTEEN = create("Sixteen").matchesAnyOf("SIXTEEN").addTo(teensDigits);

    public final Token SEVENTEEN = create("Seventeen").matchesAnyOf("SEVENTEEN").addTo(teensDigits);

    public final Token EIGHTEEN = create("Eighteen").matchesAnyOf("EIGHTEEN").addTo(teensDigits);

    public final Token NINETEEN = create("Nineteen").matchesAnyOf("NINETEEN").addTo(teensDigits);

    public final Token TWENTY = create("Twenty").matchesAnyOf("TWENTY").addTo(tensDigits);

    public final Token THIRTY = create("Thirty").matchesAnyOf("THIRTY").addTo(tensDigits);

    public final Token FORTY = create("Forty").matchesAnyOf("FORTY", "FOURTY").addTo(tensDigits);

    public final Token FIFTY = create("Fifty").matchesAnyOf("FIFTY").addTo(tensDigits);

    public final Token SIXTY = create("Sixty").matchesAnyOf("SIXTY").addTo(tensDigits);

    public final Token SEVENTY = create("Seventy").matchesAnyOf("SEVENTY").addTo(tensDigits);

    public final Token EIGHTY = create("Eighty").matchesAnyOf("EIGHTY").addTo(tensDigits);

    public final Token NINETY = create("Ninety").matchesAnyOf("NINETY").addTo(tensDigits);

    public final Token HUNDRED = create("Hundred").matchesAnyOf("HUNDRED");

    public final Token THOUSAND = create("Thousand").matchesAnyOf("THOUSAND");

    public final Token ST = create("st").matchesAnyOf("ST").addTo(numericOrdinalSuffixes);

    public final Token ND = create("nd").matchesAnyOf("ND").addTo(numericOrdinalSuffixes);

    public final Token RD = create("rd").matchesAnyOf("RD").addTo(numericOrdinalSuffixes);

    public final Token TH = create("th").matchesAnyOf("TH").addTo(numericOrdinalSuffixes);

    public final Token FIRST = create("First").matchesAnyOf("FIRST").addTo(ordinals);

    public final Token SECOND = create("Second").matchesAnyOf("SECOND").addTo(ordinals);

    public final Token THIRD = create("Third").matchesAnyOf("THIRD").addTo(ordinals);

    public final Token FOURTH = create("Fourth").matchesAnyOf("FOURTH").addTo(ordinals);

    // Numeric Ordinals (1st, 2nd, 3rd, 4th, ...)

    public final Token FIFTH = create("Fifth").matchesAnyOf("FIFTH").addTo(ordinals);

    public final Token SIXTH = create("Sixth").matchesAnyOf("SIXTH").addTo(ordinals);

    public final Token SEVENTH = create("Seventh").matchesAnyOf("SEVENTH").addTo(ordinals);

    public final Token EIGHTH = create("Eighth").matchesAnyOf("EIGHTH").addTo(ordinals);

    // Ordinals

    public final Token NINTH = create("Ninth").matchesAnyOf("NINTH").addTo(ordinals);

    public final Token TENTH = create("Tenth").matchesAnyOf("TENTH").addTo(ordinals);

    public final Token ELEVENTH = create("Eleventh").matchesAnyOf("ELEVENTH").addTo(ordinals);

    public final Token TWELFTH = create("Twelfth").matchesAnyOf("TWELFTH").addTo(ordinals);

    public final Token THIRTEENTH = create("Thirteenth").matchesAnyOf("THIRTEENTH").addTo(ordinals);

    public final Token FOURTEENTH = create("Fourteenth").matchesAnyOf("FOURTEENTH").addTo(ordinals);

    public final Token FIFTEENTH = create("Fifteenth").matchesAnyOf("FIFTEENTH").addTo(ordinals);

    public final Token SIXTEENTH = create("Sixteenth").matchesAnyOf("SIXTEENTH").addTo(ordinals);

    public final Token SEVENTEENTH = create("Seventeenth").matchesAnyOf("SEVENTEENTH").addTo(ordinals);

    public final Token EIGHTEENTH = create("Eighteenth").matchesAnyOf("EIGHTEENTH").addTo(ordinals);

    public final Token NINETEENTH = create("Nineteenth").matchesAnyOf("NINETEENTH").addTo(ordinals);

    public final Token TWENTIETH = create("Twentieth").matchesAnyOf("TWENTIETH").addTo(ordinals);

    public final Token THIRTIETH = create("Thirtieth").matchesAnyOf("THIRTIETH").addTo(ordinals);

    public final Token FORTIETH = create("Fortieth").matchesAnyOf("FORTIETH").addTo(ordinals);

    public final Token FIFTIETH = create("Fiftieth").matchesAnyOf("FIFTIETH").addTo(ordinals);

    public final Token SIXTIETH = create("Sixtieth").matchesAnyOf("SIXTIETH").addTo(ordinals);

    public final Token SEVENTIETH = create("Seventieth").matchesAnyOf("SEVENTIETH").addTo(ordinals);

    public final Token EIGHTIETH = create("Eightieth").matchesAnyOf("EIGHTIETH").addTo(ordinals);

    public final Token NINETIETH = create("Ninetieth").matchesAnyOf("NINETIETH").addTo(ordinals);

    public final Token HUNDREDTH = create("Hundredth").matchesAnyOf("HUNDREDTH").addTo(ordinals);

    public final Token THOUSANDTH = create("Thousandth").matchesAnyOf("THOUSANDTH").addTo(ordinals);

    public final Token INTERSTATE = create("Interstate").addTo(roadTypes);

    public final Token ALLEY = create("Alley")
            .matchesAnyOf("ALLEY", "ALLEE", "ALY", "ALLY")
            .addTo(roadTypes);

    public final Token ANNEX = create("Annex")
            .matchesAnyOf("ANNEX", "ANEX", "ANX", "ANNX")
            .addTo(roadTypes);

    public final Token ARCADE = create("Arcade")
            .matchesAnyOf("ARCADE", "ARC")
            .addTo(roadTypes);

    public final Token AVENUE = create("Avenue")
            .matchesAnyOf("AVENUE", "AVENU", "AVEN", "AVNUE", "AVN", "AVE", "AV")
            .addTo(roadTypes);

    public final Token BAYOU = create("Bayou")
            .matchesAnyOf("BAYOU", "BAYOO", "BYU")
            .addTo(roadTypes);

    public final Token BEACH = create("Beach")
            .matchesAnyOf("BEACH", "BCH")
            .addTo(roadTypes);

    public final Token BEND = create("Bend")
            .matchesAnyOf("BEND", "BND")
            .addTo(roadTypes);

    public final Token BLUFF = create("Bluff")
            .matchesAnyOf("BLUFF", "BLUF", "BLF")
            .addTo(roadTypes);

    public final Token BLUFFS = create("Bluffs")
            .matchesAnyOf("BLUFFS", "BLFS")
            .addTo(roadTypes);

    // Road Types

    public final Token BOTTOM = create("Bottom")
            .matchesAnyOf("BOTTOM", "BOTTM", "BTM", "BOT")
            .addTo(roadTypes);

    public final Token BOULEVARD = create("Boulevard")
            .matchesAnyOf("BOULEVARD", "BOULV", "BOUL", "BLVD", "BL")
            .addTo(roadTypes);

    public final Token BRANCH = create("Branch")
            .matchesAnyOf("BRANCH", "BRNCH")
            .addTo(roadTypes);

    public final Token BRIDGE = create("Bridge")
            .matchesAnyOf("BRIDGE", "BRDGE", "BRG", "BR")
            .addTo(roadTypes);

    public final Token BROOK = create("Brook")
            .matchesAnyOf("BROOK", "BRK")
            .addTo(roadTypes);

    public final Token BROOKS = create("Brooks")
            .matchesAnyOf("BROOKS", "BRKS")
            .addTo(roadTypes);

    public final Token BURG = create("Burg")
            .matchesAnyOf("BURG", "BG")
            .addTo(roadTypes);

    public final Token BURGS = create("Burgs")
            .matchesAnyOf("BURGS", "BGS")
            .addTo(roadTypes);

    public final Token BYPASS = create("Bypass")
            .matchesAnyOf("BYPASS", "BYPAS", "BYPA", "BYPS", "BYP")
            .addTo(roadTypes);

    public final Token CAMP = create("Camp")
            .matchesAnyOf("CAMP", "CMP", "CP")
            .addTo(roadTypes);

    public final Token CANYON = create("Canyon")
            .matchesAnyOf("CANYON", "CNYN", "CYN")
            .addTo(roadTypes);

    public final Token CAPE = create("Cape")
            .matchesAnyOf("CAPE", "CPE")
            .addTo(roadTypes);

    public final Token CAUSEWAY = create("Causeway")
            .matchesAnyOf("CAUSEWAY", "CAUSWAY", "CSWY")
            .addTo(roadTypes);

    public final Token CENTER = create("Center")
            .matchesAnyOf("CENTER", "CENTRE", "CENTR", "CENT", "CEN", "CNTER", "CNTR", "CTR")
            .addTo(roadTypes);

    public final Token CENTERS = create("Centers")
            .matchesAnyOf("CENTERS", "CTRS")
            .addTo(roadTypes);

    public final Token CIRCLE = create("Circle")
            .matchesAnyOf("CIRCLE", "CIRCL", "CIRC", "CRCLE", "CRCL", "CIR")
            .addTo(roadTypes);

    public final Token CIRCLES = create("Circles")
            .matchesAnyOf("CIRCLES", "CIRS")
            .addTo(roadTypes);

    public final Token CLIFF = create("Cliff")
            .matchesAnyOf("CLIFF", "CLF")
            .addTo(roadTypes);

    public final Token CLIFFS = create("Cliffs")
            .matchesAnyOf("CLIFFS", "CLFS")
            .addTo(roadTypes);

    public final Token CLUB = create("Club")
            .matchesAnyOf("CLUB", "CLB")
            .addTo(roadTypes);

    public final Token COMMON = create("Common")
            .matchesAnyOf("COMMON", "CMN")
            .addTo(roadTypes);

    public final Token CONNECTOR = create("Connector")
            .matchesAnyOf("CONNECTOR", "CONN")
            .addTo(roadTypes);

    public final Token CORNER = create("Corner")
            .matchesAnyOf("CORNER", "CNR", "COR")
            .addTo(roadTypes);

    public final Token CORNERS = create("Corners")
            .matchesAnyOf("CORNERS", "CORS")
            .addTo(roadTypes);

    public final Token COURSE = create("Course")
            .matchesAnyOf("COURSE", "CRSE")
            .addTo(roadTypes);

    public final Token COURT = create("Court")
            .matchesAnyOf("COURT", "CRT", "CT")
            .addTo(roadTypes);

    public final Token COURTS = create("Courts")
            .matchesAnyOf("COURTS", "CTS")
            .addTo(roadTypes);

    public final Token COVE = create("Cove")
            .matchesAnyOf("COVE", "CV")
            .addTo(roadTypes);

    public final Token COVES = create("Coves")
            .matchesAnyOf("COVES", "CVS")
            .addTo(roadTypes);

    public final Token CREEK = create("Creek")
            .matchesAnyOf("CREEK", "CR", "CRK")
            .addTo(roadTypes);

    public final Token CRESCENT = create("Crescent")
            .matchesAnyOf("CRESCENT", "CRECENT", "CRESENT", "CRES", "CRSCNT", "CRSENT", "CRSNT")
            .addTo(roadTypes);

    public final Token CREST = create("Crest")
            .matchesAnyOf("CREST", "CRST")
            .addTo(roadTypes);

    public final Token CROSSING = create("Crossing")
            .matchesAnyOf("CROSSING", "CRSSING", "XING")
            .addTo(roadTypes);

    public final Token CROSSROAD = create("Crossroad")
            .matchesAnyOf("CROSSROAD", "XRD")
            .addTo(roadTypes);

    public final Token CURVE = create("Curve")
            .matchesAnyOf("CURVE", "CURV")
            .addTo(roadTypes);

    public final Token DALE = create("Dale")
            .matchesAnyOf("DALE", "DL")
            .addTo(roadTypes);

    public final Token DAM = create("Dam")
            .matchesAnyOf("DAM", "DM")
            .addTo(roadTypes);

    public final Token DIVIDE = create("Divide")
            .matchesAnyOf("DIVIDE", "DIV", "DVD", "DV")
            .addTo(roadTypes);

    public final Token DRIVE = create("Drive")
            .matchesAnyOf("DRIVE", "DRIV", "DRV", "DR")
            .addTo(roadTypes);

    public final Token DRIVEWAY = create("Driveway")
            .matchesAnyOf("DRIVEWAY", "DRIVWY", "DRVWY")
            .addTo(roadTypes);

    public final Token DRIVES = create("Drives")
            .matchesAnyOf("DRIVES", "DRS")
            .addTo(roadTypes);

    public final Token ESTATE = create("Estate")
            .matchesAnyOf("ESTATE", "EST")
            .addTo(roadTypes);

    public final Token ESTATES = create("Estates")
            .matchesAnyOf("ESTATES", "ESTS")
            .addTo(roadTypes);

    public final Token EXPRESSWAY = create("Expressway")
            .matchesAnyOf("EXPRESSWAY", "EXPRESS", "EXPWY", "EXPR", "EXPW", "EXP", "EXPY")
            .addTo(roadTypes);

    public final Token EXTENSION = create("Extension")
            .matchesAnyOf("EXTENSION", "EXTNSN", "EXTN", "EXT")
            .addTo(roadTypes);

    public final Token EXTENSIONS = create("Extensions")
            .matchesAnyOf("EXTENSIONS", "EXTS")
            .addTo(roadTypes);

    public final Token FALL = create("Fall")
            .matchesAnyOf("FALL", "FALL")
            .addTo(roadTypes);

    public final Token FALLS = create("Falls")
            .matchesAnyOf("FALLS", "FLS")
            .addTo(roadTypes);

    public final Token FERRY = create("Ferry")
            .matchesAnyOf("FERRY", "FRRY", "FRY")
            .addTo(roadTypes);

    public final Token FIELD = create("Field")
            .matchesAnyOf("FIELD", "FLD")
            .addTo(roadTypes);

    public final Token FIELDS = create("Fields")
            .matchesAnyOf("FIELDS", "FLDS")
            .addTo(roadTypes);

    public final Token FLAT = create("Flat")
            .matchesAnyOf("FLAT", "FLT")
            .addTo(roadTypes);

    public final Token FLATS = create("Flats")
            .matchesAnyOf("FLATS", "FLTS")
            .addTo(roadTypes);

    public final Token FORD = create("Ford")
            .matchesAnyOf("FORD", "FRD")
            .addTo(roadTypes);

    public final Token FORDS = create("Fords")
            .matchesAnyOf("FORDS", "FRDS")
            .addTo(roadTypes);

    public final Token FOREST = create("Forest")
            .matchesAnyOf("FOREST", "FRST")
            .addTo(roadTypes);

    public final Token FORESTS = create("Forests")
            .matchesAnyOf("FORESTS", "FRSTS")
            .addTo(roadTypes);

    public final Token FORGE = create("Forge")
            .matchesAnyOf("FORGE", "FORG", "FRG")
            .addTo(roadTypes);

    public final Token FORGES = create("Forges")
            .matchesAnyOf("FORGES", "FRGS")
            .addTo(roadTypes);

    public final Token FORK = create("Fork")
            .matchesAnyOf("FORK", "FRK", "FK")
            .addTo(roadTypes);

    public final Token FORKS = create("Forks")
            .matchesAnyOf("FORKS", "FRKS")
            .addTo(roadTypes);

    public final Token FORT = create("Fort")
            .matchesAnyOf("FORT", "FT")
            .addTo(roadTypes);

    public final Token FREEWAY = create("Freeway")
            .matchesAnyOf("FREEWAY", "FREEWY", "FRWAY", "FRWY", "FWY")
            .addTo(roadTypes);

    public final Token GARDEN = create("Garden")
            .matchesAnyOf("GARDEN", "GDN")
            .addTo(roadTypes);

    public final Token GARDENS = create("Gardens")
            .matchesAnyOf("GARDENS", "GRDNS", "GDNS")
            .addTo(roadTypes);

    public final Token GATEWAY = create("Gateway")
            .matchesAnyOf("GATEWAY", "GATEWY", "GATWAY", "GTWAY", "GTWY")
            .addTo(roadTypes);

    public final Token GLEN = create("Glen")
            .matchesAnyOf("GLEN", "GLN")
            .addTo(roadTypes);

    public final Token GLENS = create("Glens")
            .matchesAnyOf("GLENS", "GLNS")
            .addTo(roadTypes);

    public final Token GREEN = create("Green")
            .matchesAnyOf("GREEN", "GRN")
            .addTo(roadTypes);

    public final Token GREENS = create("Greens")
            .matchesAnyOf("GREENS", "GRNS")
            .addTo(roadTypes);

    public final Token GROVE = create("Grove")
            .matchesAnyOf("GROVE", "GROV", "GRV")
            .addTo(roadTypes);

    public final Token GROVES = create("Groves")
            .matchesAnyOf("GROVES", "GRVS")
            .addTo(roadTypes);

    public final Token HARBOR = create("Harbor")
            .matchesAnyOf("HARBOR", "HARB", "HARBR", "HBR")
            .addTo(roadTypes);

    public final Token HARBORS = create("Harbors")
            .matchesAnyOf("HARBORS", "HBRS")
            .addTo(roadTypes);

    public final Token HAVEN = create("Haven")
            .matchesAnyOf("HAVEN", "HAVN", "HVN")
            .addTo(roadTypes);

    public final Token Height = create("Height")
            .matchesAnyOf("Height")
            .addTo(roadTypes);

    public final Token HeightS = create("Heights")
            .matchesAnyOf("HeightS", "HATS", "HGTS", "HTS", "HT")
            .addTo(roadTypes);

    public final Token HIGHWAY = create("Highway")
            .matchesAnyOf("HIGHWAY", "HIGHWY", "HIWAY", "HIWY", "HWAY", "HWY")
            .addTo(roadTypes);

    public final Token HILL = create("Hill")
            .matchesAnyOf("HILL", "HL")
            .addTo(roadTypes);

    public final Token HILLS = create("Hills")
            .matchesAnyOf("HILLS", "HLS")
            .addTo(roadTypes);

    public final Token HOLLOW = create("Hollow")
            .matchesAnyOf("HOLLOW", "HOLW")
            .addTo(roadTypes);

    public final Token HOLLOWS = create("Hollows")
            .matchesAnyOf("HOLLOWS", "HOLWS")
            .addTo(roadTypes);

    public final Token INLET = create("Inlet")
            .matchesAnyOf("INLET", "INLT")
            .addTo(roadTypes);

    public final Token ISLAND = create("Island")
            .matchesAnyOf("ISLAND", "ISLND", "IS")
            .addTo(roadTypes);

    public final Token ISLANDS = create("Islands")
            .matchesAnyOf("ISLANDS", "ISLNDS", "ISS")
            .addTo(roadTypes);

    public final Token JUNCTION = create("Junction")
            .matchesAnyOf("JUNCTION", "JCTION", "JCTN", "JUNCTN", "JUNCTON", "JCT")
            .addTo(roadTypes);

    public final Token JUNCTIONS = create("Junctions")
            .matchesAnyOf("JUNCTIONS", "JCTNS", "JCTS")
            .addTo(roadTypes);

    public final Token KEY = create("Key")
            .matchesAnyOf("KEY", "KY")
            .addTo(roadTypes);

    public final Token KEYS = create("Keys")
            .matchesAnyOf("KEYS", "KYS")
            .addTo(roadTypes);

    public final Token KNOLL = create("Knoll")
            .matchesAnyOf("KNOLL", "KNOL", "KNL")
            .addTo(roadTypes);

    public final Token KNOLLS = create("Knolls")
            .matchesAnyOf("KNOLLS", "KNLS")
            .addTo(roadTypes);

    public final Token LAKE = create("Lake")
            .matchesAnyOf("LAKE", "LK")
            .addTo(roadTypes);

    public final Token LAKES = create("Lakes")
            .matchesAnyOf("LAKES", "LKS")
            .addTo(roadTypes);

    public final Token LANDING = create("Landing")
            .matchesAnyOf("LANDING", "LNDNG", "LNDG")
            .addTo(roadTypes);

    public final Token LANE = create("Lane")
            .matchesAnyOf("LANE", "LN")
            .addTo(roadTypes);

    public final Token LANES = create("Lanes")
            .matchesAnyOf("LANES", "LNS")
            .addTo(roadTypes);

    public final Token LIGHT = create("Light")
            .matchesAnyOf("LIGHT", "LGT")
            .addTo(roadTypes);

    public final Token LIGHTS = create("Lights")
            .matchesAnyOf("LIGHTS", "LGTS")
            .addTo(roadTypes);

    public final Token LOAF = create("Loaf")
            .matchesAnyOf("LOAF", "LF")
            .addTo(roadTypes);

    public final Token LOCK = create("Monitor")
            .matchesAnyOf("LOCK", "LCK")
            .addTo(roadTypes);

    public final Token LOCKS = create("Locks")
            .matchesAnyOf("LOCKS", "LCKS")
            .addTo(roadTypes);

    public final Token LODGE = create("Lodge")
            .matchesAnyOf("LODGE", "LODG", "LDG")
            .addTo(roadTypes);

    public final Token LOOP = create("Loop")
            .matchesAnyOf("LOOP", "LOOPS", "LP")
            .addTo(roadTypes);

    public final Token MANOR = create("Manor")
            .matchesAnyOf("MANOR", "MNR")
            .addTo(roadTypes);

    public final Token MANORS = create("Manors")
            .matchesAnyOf("MANORS", "MNRS")
            .addTo(roadTypes);

    public final Token MEADOW = create("Meadow")
            .matchesAnyOf("MEADOW", "MDW")
            .addTo(roadTypes);

    public final Token MEADOWS = create("Meadows")
            .matchesAnyOf("MEADOWS", "MEDOWS", "MDWS")
            .addTo(roadTypes);

    public final Token MILL = create("Mill")
            .matchesAnyOf("MILL", "ML")
            .addTo(roadTypes);

    public final Token MILLS = create("Mills")
            .matchesAnyOf("MILLS", "MLS")
            .addTo(roadTypes);

    public final Token MISSION = create("Mission")
            .matchesAnyOf("MISSION", "MSN")
            .addTo(roadTypes);

    public final Token MOTORWAY = create("Motorway")
            .matchesAnyOf("MOTORWAY", "MTWY")
            .addTo(roadTypes);

    public final Token MOUNT = create("Mount")
            .matchesAnyOf("MOUNT", "MT")
            .addTo(roadTypes);

    public final Token MOUNTAIN = create("Mountain")
            .matchesAnyOf("MOUNTAIN", "MNTAIN", "MTN")
            .addTo(roadTypes);

    public final Token MOUNTAINS = create("Mountains")
            .matchesAnyOf("MOUNTAINS", "MNTNS", "MTNS")
            .addTo(roadTypes);

    public final Token NECK = create("Neck")
            .matchesAnyOf("NECK", "NCK")
            .addTo(roadTypes);

    public final Token ORCHARD = create("Orchard")
            .matchesAnyOf("ORCHARD", "ORCHRD", "ORCH")
            .addTo(roadTypes);

    public final Token OVAL = create("Oval")
            .matchesAnyOf("OVAL", "OVL")
            .addTo(roadTypes);

    public final Token OVERPASS = create("Overpass")
            .matchesAnyOf("OVERPASS", "OPAS")
            .addTo(roadTypes);

    public final Token PARKWAY = create("Parkway")
            .matchesAnyOf("PARKWAY", "PARKWY", "PKWAY", "PKWY", "PKY")
            .addTo(roadTypes);

    public final Token PARKWAYS = create("Parkways")
            .matchesAnyOf("PARKWAYS", "PKWYS")
            .addTo(roadTypes);

    public final Token PASSAGE = create("Passage")
            .matchesAnyOf("PASSAGE", "PSGE")
            .addTo(roadTypes);

    public final Token PATH = create("Path")
            .matchesAnyOf("PATH")
            .addTo(roadTypes);

    public final Token PATHS = create("Paths")
            .matchesAnyOf("PATHS")
            .addTo(roadTypes);

    public final Token PINE = create("Pine")
            .matchesAnyOf("PINE", "PNE")
            .addTo(roadTypes);

    public final Token PINES = create("Pines")
            .matchesAnyOf("PINES", "PNES")
            .addTo(roadTypes);

    public final Token PLACE = create("Place")
            .matchesAnyOf("PLACE", "PL")
            .addTo(roadTypes);

    public final Token PLAIN = create("Plain")
            .matchesAnyOf("PLAIN", "PLN")
            .addTo(roadTypes);

    public final Token PLAINS = create("Plains")
            .matchesAnyOf("PLAINS", "PLNS")
            .addTo(roadTypes);

    public final Token PLAZA = create("Plaza")
            .matchesAnyOf("PLAZA", "PLZA", "PLZ")
            .addTo(roadTypes);

    public final Token POINT = create("Point")
            .matchesAnyOf("POINT", "PT")
            .addTo(roadTypes);

    public final Token POINTS = create("Points")
            .matchesAnyOf("POINTS", "PTS")
            .addTo(roadTypes);

    public final Token PORT = create("Port")
            .matchesAnyOf("PORT", "PRT")
            .addTo(roadTypes);

    public final Token PORTS = create("Ports")
            .matchesAnyOf("PORTS", "PRTS")
            .addTo(roadTypes);

    public final Token PRAIRIE = create("Prairie")
            .matchesAnyOf("PRAIRIE", "PR")
            .addTo(roadTypes);

    public final Token RADIAL = create("Radial")
            .matchesAnyOf("RADIAL", "RADL", "RADIEL", "RAD")
            .addTo(roadTypes);

    public final Token RANCH = create("Ranch")
            .matchesAnyOf("RANCH", "RNCH")
            .addTo(roadTypes);

    public final Token RANCHES = create("Ranches")
            .matchesAnyOf("RANCHES", "RNCHS")
            .addTo(roadTypes);

    public final Token RAPID = create("Rapid")
            .matchesAnyOf("RAPID", "RPD")
            .addTo(roadTypes);

    public final Token RAPIDS = create("Rapids")
            .matchesAnyOf("RAPIDS", "RPDS")
            .addTo(roadTypes);

    public final Token REST = create("Rest")
            .matchesAnyOf("REST", "RST")
            .addTo(roadTypes);

    public final Token RIDGE = create("Ridge")
            .matchesAnyOf("RIDGE", "RDGE", "RDG")
            .addTo(roadTypes);

    public final Token RIVER = create("River")
            .matchesAnyOf("RIVER", "RIV")
            .addTo(roadTypes);

    public final Token ROAD = create("Road")
            .matchesAnyOf("ROAD")
            .addTo(roadTypes);

    public final Token ROADS = create("Roads")
            .matchesAnyOf("ROADS", "RDS")
            .addTo(roadTypes);

    public final Token ROUTE = create("Route")
            .matchesAnyOf("ROUTE", "RTE", "RT", "RN")
            .addTo(roadTypes);

    public final Token SHOAL = create("Shoal")
            .matchesAnyOf("SHOAL", "SHL")
            .addTo(roadTypes);

    public final Token SHOALS = create("Shoals")
            .matchesAnyOf("SHOALS", "SHLS")
            .addTo(roadTypes);

    public final Token SHORE = create("Shore")
            .matchesAnyOf("SHORE", "SHR")
            .addTo(roadTypes);

    public final Token SHORES = create("Shores")
            .matchesAnyOf("SHORES", "SHRS")
            .addTo(roadTypes);

    public final Token SKYWAY = create("Skyway")
            .matchesAnyOf("SKYWAY", "SKYWY", "SKWY")
            .addTo(roadTypes);

    public final Token SPRING = create("Spring")
            .matchesAnyOf("SPRING", "SPNG", "SPG")
            .addTo(roadTypes);

    public final Token SPRINGS = create("Springs")
            .matchesAnyOf("SPRINGS", "SPNGS", "SPGS")
            .addTo(roadTypes);

    public final Token SPUR = create("Spur")
            .matchesAnyOf("SPUR")
            .addTo(roadTypes);

    public final Token SPURS = create("Spurs")
            .matchesAnyOf("SPURS")
            .addTo(roadTypes);

    public final Token SQUARE = create("Square")
            .matchesAnyOf("SQUARE", "SQRE", "SQU", "SQR", "SQ")
            .addTo(roadTypes);

    public final Token SQUARES = create("Squares")
            .matchesAnyOf("SQUARES", "SQRS", "SQS")
            .addTo(roadTypes);

    public final Token STATEROUTE = create("Stateroute")
            .matchesAnyOf("SR")
            .addTo(roadTypes);

    public final Token STATION = create("Station")
            .matchesAnyOf("STATION", "STATN", "STA", "STN")
            .addTo(roadTypes);

    public final Token STRAVENUE = create("Stravenue")
            .matchesAnyOf("STRAVENUE", "STRAVEN", "STRAVE", "STRAVN", "STRAV", "STRA", "STRVNUE", "STRVN")
            .addTo(roadTypes);

    public final Token STREAM = create("Stream")
            .matchesAnyOf("STREAM", "STRM")
            .addTo(roadTypes);

    public final Token STREET = create("Street")
            .matchesAnyOf("STREET", "STRT", "STR")
            .addTo(roadTypes);

    public final Token STREETS = create("Streets")
            .matchesAnyOf("STS")
            .addTo(roadTypes);

    public final Token SUMMIT = create("Summit")
            .matchesAnyOf("SUMMIT", "SMT")
            .addTo(roadTypes);

    public final Token TERRACE = create("Terrace")
            .matchesAnyOf("TERRACE", "TERR", "TER")
            .addTo(roadTypes);

    public final Token THROUGHWAY = create("Throughway")
            .matchesAnyOf("THROUGHWAY", "THRWY", "TRWY", "TWY")
            .addTo(roadTypes);

    public final Token TRACE = create("Trace")
            .matchesAnyOf("TRACE", "TRCE")
            .addTo(roadTypes);

    public final Token TRACES = create("Traces")
            .matchesAnyOf("TRACES")
            .addTo(roadTypes);

    public final Token TRACK = create("Track")
            .matchesAnyOf("TRACK", "TRAK", "TRK")
            .addTo(roadTypes);

    public final Token TRACKS = create("Tracks")
            .matchesAnyOf("TRACKS", "TRKS")
            .addTo(roadTypes);

    public final Token TRAFFICWAY = create("Trafficway")
            .matchesAnyOf("TRAFFICWAY", "TRFWY", "TRFY")
            .addTo(roadTypes);

    public final Token TRAIL = create("Trail")
            .matchesAnyOf("TRAIL", "TRL", "TR")
            .addTo(roadTypes);

    public final Token TRAILS = create("Trails")
            .matchesAnyOf("TRAILS", "TRLS")
            .addTo(roadTypes);

    public final Token TUNNEL = create("Tunnel")
            .matchesAnyOf("TUNNEL", "TUNNL", "TUNEL", "TUNL")
            .addTo(roadTypes);

    public final Token TUNNELS = create("Tunnels")
            .matchesAnyOf("TUNNELS", "TUNLS")
            .addTo(roadTypes);

    public final Token TURNPIKE = create("Turnpike")
            .matchesAnyOf("TURNPIKE", "TPKE")
            .addTo(roadTypes);

    public final Token UNDERPASS = create("Underpass")
            .matchesAnyOf("UNDERPASS", "UPAS")
            .addTo(roadTypes);

    public final Token UNION = create("Union")
            .matchesAnyOf("UNION", "UN")
            .addTo(roadTypes);

    public final Token UNIONS = create("Unions")
            .matchesAnyOf("UNIONS", "UNS")
            .addTo(roadTypes);

    public final Token VALLEY = create("Valley")
            .matchesAnyOf("VALLEY", "VLY")
            .addTo(roadTypes);

    public final Token VALLEYS = create("Valleys")
            .matchesAnyOf("VALLEYS", "VLYS")
            .addTo(roadTypes);

    public final Token VIADUCT = create("Viaduct")
            .matchesAnyOf("VIADUCT", "VDCT", "VIADCT", "VIA")
            .addTo(roadTypes);

    public final Token VIEW = create("View")
            .matchesAnyOf("VIEW", "VW")
            .addTo(roadTypes);

    public final Token VIEWS = create("Views")
            .matchesAnyOf("VIEWS", "VWS")
            .addTo(roadTypes);

    public final Token VILLAGE = create("Village")
            .matchesAnyOf("VILLAGE", "VLG")
            .addTo(roadTypes);

    public final Token VILLAGES = create("Villages")
            .matchesAnyOf("VILLAGES", "VLGS")
            .addTo(roadTypes);

    public final Token VILLE = create("Ville")
            .matchesAnyOf("VILLE", "VL")
            .addTo(roadTypes);

    public final Token VISTA = create("Vista")
            .matchesAnyOf("VISTA", "VIS")
            .addTo(roadTypes);

    public final Token WAY = create("Way")
            .matchesAnyOf("WAY", "WY")
            .addTo(roadTypes);

    public final Token WELL = create("Well")
            .matchesAnyOf("WELL", "WL")
            .addTo(roadTypes);

    public final Token WELLS = create("Wells")
            .matchesAnyOf("WELLS", "WLS")
            .addTo(roadTypes);

    public final Token AND = create("and").matchesAnyOf("and", "&");

    public final Token OPEN_PARENTHESIS = create("OpenParenthesis").matchesAnyOf("(");

    public final Token CLOSE_PARENTHESIS = create("CloseParenthesis").matchesAnyOf(")");

    public boolean isCardinalDirection(final Token token)
    {
        return cardinalDirections.contains(token);
    }

    public boolean isCloseParenthesis(final Token token)
    {
        return matches(token, CLOSE_PARENTHESIS);
    }

    public boolean isDigit(final Token token)
    {
        return matches(token, DIGIT);
    }

    public boolean isInterstate(final Token token)
    {
        return matches(token, INTERSTATE);
    }

    public boolean isNamedDigit(final Token token)
    {
        return isOnesDigit(token) || isTeensDigit(token) || isTensDigit(token);
    }

    public boolean isNumericOrdinalSuffix(final Token token)
    {
        return numericOrdinalSuffixes.contains(token);
    }

    public boolean isOnesDigit(final Token token)
    {
        return onesDigits.contains(token);
    }

    public boolean isOpenParenthesis(final Token token)
    {
        return matches(token, OPEN_PARENTHESIS);
    }

    public boolean isOrdinal(final Token token)
    {
        return ordinals.contains(token);
    }

    public boolean isQuadrant(final Token token)
    {
        return quadrants.contains(token);
    }

    public boolean isRoadType(final Token token)
    {
        return roadTypes.contains(token);
    }

    public boolean isTeensDigit(final Token token)
    {
        return teensDigits.contains(token);
    }

    public boolean isTensDigit(final Token token)
    {
        return tensDigits.contains(token);
    }

    protected boolean lookingAtHighwayDesignator(final SymbolStream stream)
    {
        return stream.lookingAtDigit();
    }

    protected Set<Token> roadTypes()
    {
        return roadTypes;
    }

    private boolean matches(final Token token, final Token expected)
    {
        return token != null && token.equals(expected);
    }

    {
        // INTERSTATE HIGHWAY 5, INTERSTATE 99
        INTERSTATE.matches("INTERSTATE", (stream) ->
        {
            stream.next();
            stream.skipAnyWhiteSpace();
            stream.skipAny(Symbol.of("HIGHWAY"));
            stream.skipAnyWhiteSpace();
            if (lookingAtHighwayDesignator(stream))
            {
                return INTERSTATE.of(stream);
            }
            return null;
        });

        // I5, I-5, I - 405, I-401A
        INTERSTATE.matches("I", (stream) ->
        {
            stream.next();
            stream.skipAnyWhiteSpace();
            stream.skipAny(Symbol.DASH);
            stream.skipAnyWhiteSpace();
            if (lookingAtHighwayDesignator(stream))
            {
                return INTERSTATE.of(stream);
            }
            return null;
        });
    }
}
