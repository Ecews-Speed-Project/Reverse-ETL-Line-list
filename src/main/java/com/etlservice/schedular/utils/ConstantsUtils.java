/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.etlservice.schedular.utils;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author MORRISON.I
 */
public class ConstantsUtils {
    public static final int PHARMACY_ENCOUNTER_TYPE = 13;
    public static final int CARE_CARD_ENCOUNTER_TYPE = 12;
    public static final int LAB_ENCOUNTER_TYPE = 11;
    public static final int SAMPLE_COLLECTION_TYPE = 31;
    public static final int ENROLLMENT_TYPE = 14;

    public static final  int IPT_TYPE = 23;

    public static final  int GENERAL_ANTENATAL_CARE_TYPE = 10;
    public static final  int ART_COMMENCEMENT_TYPE = 25;

    public static final  String AGE_TYPE_YEARS = "YEARS";
    public static final  String AGE_TYPE_MONTHS = "MONTHS";

    //Form
    public static final  int ENROLLMENT_FORM = 23;
    public static final  int PHARMACY_FORM = 27;
    public static final int SAMPLE_COLLECTION_FORM = 67;
    public static final int OTZ_FORM = 73;
    public static final int CLIENT_INTAKE_FORM = 10;
    public static final int LABORATORY_ORDER_AND_RESULT_FORM = 21;
    public static final int CARE_CARD = 14;
    public static final int CLIENT_TRACKING_AND_TERMINATION_FORM = 13;
    public static final  int GENERAL_ANTENATAL_CARE_FORM = 16;
    public static final  int ART_COMMENCEMENT_FORM = 56;
    public static final  int IPT_FORM = 53;
    public static final String ACTIVE_STATUS = "Active";
    public static final String INACTIVE_STATUS = "InActive";
    public static final String FULL_GENDER_FEMALE = "Female";
    public static final String FULL_GENDER_MALE = "Male";
    public static final String SHORT_GENDER_FEMALE = "F";
    public static final String SHORT_GENDER_MALE = "M";
    private ConstantsUtils() {
        throw new IllegalStateException("Utility class");
    }

}
