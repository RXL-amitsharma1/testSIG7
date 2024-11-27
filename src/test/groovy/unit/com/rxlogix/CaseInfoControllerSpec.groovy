package unit.com.rxlogix

import com.rxlogix.CaseInfoController
import com.rxlogix.CaseInfoService
import com.rxlogix.DynamicReportService
import com.rxlogix.PriorityService
import com.rxlogix.dto.CaseDataDTO
import com.rxlogix.enums.ReportFormat
import com.rxlogix.signal.SingleCaseAlert
import com.rxlogix.signal.SingleOnDemandAlert
import com.rxlogix.UserService
import com.rxlogix.user.User
import com.rxlogix.signal.Justification
import com.rxlogix.WorkflowRuleService
import grails.plugin.springsecurity.SpringSecurityUtils
import grails.testing.web.controllers.ControllerUnitTest
import groovy.transform.SourceURI
import spock.lang.Specification
import java.nio.file.Path
import java.nio.file.Paths

/**
 * See the API for {@link grails.test.mixin.support.GrailsUnitTestMixin} for usage instructions
 */
class CaseInfoControllerSpec extends Specification implements ControllerUnitTest<CaseInfoController> {

    File file
    def caseDetailMap
    User user
    List signalNotInAlert
    CaseDataDTO caseDataDTO
    def caseInfoMap
    def caseInfoMapValues
    def workflowRuleService
    def dispositionMap
    def versionsList
    def actionTypeAndActionMap
    def actionConfigurationList

    def setup() {

        @SourceURI
        URI sourceUri
        Path scriptLocation = Paths.get(sourceUri)
        String directory = scriptLocation.toString().replace("AdHocAlertControllerSpec.groovy", "testingFiles/Details.html")
        file = new File(directory)
        user = new User(email: "test1@test.com", username: 'user1', createdBy: "Test", modifiedBy: "Test")
        user.preference.timeZone = 'UTC'
        user.save()
        caseDataDTO = new CaseDataDTO()
        caseDataDTO.totalCount = 6
        caseDataDTO.alertId = 245
        caseDataDTO.id = 24445
        caseDataDTO.version=1
        caseInfoMapValues = ['Case Information': [
                        ['Case Number': '20US00017192', 'Initial Receipt Date': '01-FEB-2020', 'Latest Follow-Up Date': '01-FEB-2020', 'Report Type': 'Spontaneous', 'Country': 'UNITED STATES', 'Seriousness': 'Serious', 'HCP': 'Yes', 'Pregnancy': 'NO', 'Case Seriousness': null, 'Case Classification': null, 'Combo Flag': 'No', 'containsValues': true]
                ], 'Patient Information': [
                        ['Patient ID': 'PL', 'DOB': null, 'Age with Units': '25 Years', 'Gender': null, 'Weight (kg)': null, 'Race': null, 'containsValues': true]
                ], 'Pregnancy Information': [
                        ['Pregnancy Report Type': null, 'Date of LMP': null, 'Delivery Date': null, 'Pregnancy Outcome': null, 'Fetal Outcome': null, 'Weeks of exposure': null, 'Trimester of Exposure': null, 'check_seq_num': '1', 'containsValues': false]
                ], 'Product Information': [
                        ['Product Name': 'RxLogix Tablet', 'Generic Name': 'MOEXIPRIL HYDROCHLORIDE, DIAZEPAM, SAFINAMIDE', 'Drug Type': 'Concomitant', 'Indication': 'Nasopharyngitis', 'Formulation': 'Chewable Tablet', 'Strength': '1000', 'Strength Units': 'mg', 'Manufacturer': null, 'Action Taken': null, 'check_seq_num': '1', 'containsValues': true],
                        ['Product Name': 'Test Product AJ', 'Generic Name': 'ASPIRIN ALUMINIUM', 'Drug Type': 'Suspect', 'Indication': 'Cough', 'Formulation': 'Capsule', 'Strength': '100', 'Strength Units': 'millimole', 'Manufacturer': 'Standard Pharmaceutical Co AJ', 'Action Taken': 'Dose not changed', 'check_seq_num': '1', 'containsValues': true]
                ], 'Dosage Regimen': [
                        ['Product Name': 'RxLogix Tablet', 'Therapy Start Date': null, 'Therapy Stop Date': null, 'Therapy Duration': null, 'Ongoing': null, 'Dose': null, 'Dose Unit': null, 'Daily Dose': null, 'Route': null, 'Frequency': null, 'Lot Number': null, 'Expiry Date': null, 'check_seq_num': '1', 'containsValues': true],
                        ['Product Name': 'Test Product AJ', 'Therapy Start Date': '25-JAN-2020', 'Therapy Stop Date': null, 'Therapy Duration': null, 'Ongoing': 'Yes', 'Dose': null, 'Dose Unit': null, 'Daily Dose': '3', 'Route': 'Oral', 'Frequency': null, 'Lot Number': '12345', 'Expiry Date': null, 'check_seq_num': '1', 'containsValues': true]
                ], 'Event Information': [
                        ['Event PT': 'Pyrexia', 'Reported Term': 'fever', 'Event SOC': 'General disorders and administration site conditions', 'Event HLGT': 'Body temperature conditions', 'Event HLT': 'Febrile disorders', 'Event LLT': 'Fever', 'Onset Date': null, 'Seriousness Criteria': '-/-/-/-/-/-/MS ', 'Event Seriousness': 'Yes', 'Event Outcome': null, 'Medication Error?': 'No', 'Onset Latency': null, 'check_seq_num': '1', 'containsValues': true]
                ], 'Cause Of Death Information': [
                        ['Cause Of Death Coded': null, 'Death Date': null, 'Autopsy': null, 'Autopsy Results': null, 'check_seq_num': '1', 'containsValues': false]
                ], 'Product Event Information': [
                        ['Product Name': 'Test Product AJ', 'Event PT': 'Pyrexia', 'Core Listedness': 'Unlisted', 'IB Listedness': 'Unlisted', 'Reporter Causality': null, 'Company Causality': null, 'Rechallenge': null, 'Dechallenge': null, 'Time To Onset (Days)': null, 'check_seq_num': '1', 'containsValues': true]
                ], 'Device Information': [
                        ['Product Name': null, 'Generic Name': null, 'Brand Name': null, 'Common Device Name': null, 'Malfunction': null, 'Device Usage': null, 'Product Code': null, 'Serial#': null, 'Model#': null, 'Catalog#': null, 'Product Type/Others': null, 'Manufacturer': null, 'Lot#': null, 'UDI Number': null, 'Remedial Action': null, 'Follow-up Correction': null, 'containsValues': false]
                ], 'Device Problems': [
                        ['Product Name': null, 'Generic Name': null, 'Brand Name/Common Device Name': null, 'Product Code': null, 'Product Type/Others': null, 'Device Problem Codes': null, 'containsValues': false]
                ], 'Narrative': [
                        ['Case Narrative': null, 'Case Abbreviated Narrative': null, 'containsValues': false]
                ], 'Patient Medical History': [
                        ['Patient Condition Type': null, 'Patient Medical Condition PT': null, 'Notes': null, 'Start Date': null, 'End Date': null, 'containsValues': false]
                ], 'Lab Data': [
                        ['Lab Test Name': null, 'Test Date': null, 'Lab Data Result': null, 'Lab Data Result Unit': null, 'Normal High': null, 'Normal Low': null, 'Lab Data Assessment': null, 'containsValues': false]
                ], 'Case References': [
                        ['Reference Type': null, 'Reference Number': null, 'containsValues': false]
                ], 'Study Information': [
                        ['Study Name': null, 'Project ID': null, 'Study Title': null, 'Study ID': null, 'Center ID': null, 'containsValues': false]
                ], 'Literature Information': [
                        ['Journal Title': null, 'Article Title': null, 'Author': null, 'Volume': null, 'Year': null, 'Page Number': null, 'containsValues': false]
                ], 'Versions': [
                        ['Versions': '0,1,2,3,4,5,6', 'containsValues': true]
                ]]
        caseInfoMap =['Case Information': [
                        ['Case Number': '20US00017192', 'Initial Receipt Date': '01-FEB-2020', 'Latest Follow-Up Date': '01-FEB-2020', 'Report Type': 'Spontaneous', 'Country': 'UNITED STATES', 'Seriousness': 'Serious', 'HCP': 'Yes', 'Pregnancy': 'NO', 'Case Seriousness': null, 'Case Classification': null, 'Combo Flag': 'No']
                ], 'Patient Information': [
                        ['Patient ID': 'PL', 'DOB': null, 'Age with Units': '25 Years', 'Gender': null, 'Weight (kg)': null, 'Race': null]
                ], 'Pregnancy Information': [
                        ['Pregnancy Report Type': null, 'Date of LMP': null, 'Delivery Date': null, 'Pregnancy Outcome': null, 'Fetal Outcome': null, 'Weeks of exposure': null, 'Trimester of Exposure': null, 'check_seq_num': '1']
                ], 'Product Information': [
                        ['Product Name': 'RxLogix Tablet', 'Generic Name': 'MOEXIPRIL HYDROCHLORIDE, DIAZEPAM, SAFINAMIDE', 'Drug Type': 'Concomitant', 'Indication': 'Nasopharyngitis', 'Formulation': 'Chewable Tablet', 'Strength': '1000', 'Strength Units': 'mg', 'Manufacturer': null, 'Action Taken': null, 'check_seq_num': '1'],
                        ['Product Name': 'Test Product AJ', 'Generic Name': 'ASPIRIN ALUMINIUM', 'Drug Type': 'Suspect', 'Indication': 'Cough', 'Formulation': 'Capsule', 'Strength': '100', 'Strength Units': 'millimole', 'Manufacturer': 'Standard Pharmaceutical Co AJ', 'Action Taken': 'Dose not changed', 'check_seq_num': '1']
                ], 'Dosage Regimen': [
                        ['Product Name': 'RxLogix Tablet', 'Therapy Start Date': null, 'Therapy Stop Date': null, 'Therapy Duration': null, 'Ongoing': null, 'Dose': null, 'Dose Unit': null, 'Daily Dose': null, 'Route': null, 'Frequency': null, 'Lot Number': null, 'Expiry Date': null, 'check_seq_num': '1'],
                        ['Product Name': 'Test Product AJ', 'Therapy Start Date': '25-JAN-2020', 'Therapy Stop Date': null, 'Therapy Duration': null, 'Ongoing': 'Yes', 'Dose': null, 'Dose Unit': null, 'Daily Dose': '3', 'Route': 'Oral', 'Frequency': null, 'Lot Number': '12345', 'Expiry Date': null, 'check_seq_num': '1']
                ], 'Event Information': [
                        ['Event PT': 'Pyrexia', 'Reported Term': 'fever', 'Event SOC': 'General disorders and administration site conditions', 'Event HLGT': 'Body temperature conditions', 'Event HLT': 'Febrile disorders', 'Event LLT': 'Fever', 'Onset Date': null, 'Seriousness Criteria': '-/-/-/-/-/-/MS ', 'Event Seriousness': 'Yes', 'Event Outcome': null, 'Medication Error?': 'No', 'Onset Latency': null, 'check_seq_num': '1']
                ], 'Cause Of Death Information': [
                        ['Cause Of Death Coded': null, 'Death Date': null, 'Autopsy': null, 'Autopsy Results': null, 'check_seq_num': '1']
                ], 'Product Event Information': [
                        ['Product Name': 'Test Product AJ', 'Event PT': 'Pyrexia', 'Core Listedness': 'Unlisted', 'IB Listedness': 'Unlisted', 'Reporter Causality': null, 'Company Causality': null, 'Rechallenge': null, 'Dechallenge': null, 'Time To Onset (Days)': null, 'check_seq_num': '1']
                ], 'Device Information': [
                        ['Product Name': null, 'Generic Name': null, 'Brand Name': null, 'Common Device Name': null, 'Malfunction': null, 'Device Usage': null, 'Product Code': null, 'Serial#': null, 'Model#': null, 'Catalog#': null, 'Product Type/Others': null, 'Manufacturer': null, 'Lot#': null, 'UDI Number': null, 'Remedial Action': null, 'Follow-up Correction': null]
                ], 'Device Problems': [
                        ['Product Name': null, 'Generic Name': null, 'Brand Name/Common Device Name': null, 'Product Code': null, 'Product Type/Others': null, 'Device Problem Codes': null]
                ], 'Narrative': [
                        ['Case Narrative': null, 'Case Abbreviated Narrative': null]
                ], 'Patient Medical History': [
                        ['Patient Condition Type': null, 'Patient Medical Condition PT': null, 'Notes': null, 'Start Date': null, 'End Date': null]
                ], 'Lab Data': [
                        ['Lab Test Name': null, 'Test Date': null, 'Lab Data Result': null, 'Lab Data Result Unit': null, 'Normal High': null, 'Normal Low': null, 'Lab Data Assessment': null]
                ], 'Case References': [
                        ['Reference Type': null, 'Reference Number': null]
                ], 'Study Information': [
                        ['Study Name': null, 'Project ID': null, 'Study Title': null, 'Study ID': null, 'Center ID': null]
                ], 'Literature Information': [
                        ['Journal Title': null, 'Article Title': null, 'Author': null, 'Volume': null, 'Year': null, 'Page Number': null]
                ], 'Versions': [
                        ['Versions': '0,1,2,3,4,5,6']
                ], 'versionNum': 1]
        actionConfigurationList = [
                ['value': 'Additional investigations', 'id': 7893],
                ['value': 'Communication', 'id': 7902],
                ['value': 'Exchange of Information', 'id': 7911],
                ['value': 'Other', 'id': 7920],
                ['value': 'Periodic Review', 'id': 7929],
                ['value': 'Review ICSRs', 'id': 7938]
        ]
        versionsList = [versionsList: [
                ["followUpNum": 1],
                ["followUpNum": 2],
                ["followUpNum": 3],
                ["followUpNum": 4]
        ], isLastVersionPresent: false,
           isArgusDataSource: false, previousVersion: 6, previousFollowUp: 0
        ]
        actionTypeAndActionMap =  ['actionTypeList': [
                ['id': 7625, 'value': 'AESM', 'text': 'AESM'],
                ['id': 7877, 'value': 'Further Review', 'text': 'Further Review']
        ], 'actionPropertiesMap': ['types': [
                ['id': 7625, 'value': 'AESM', 'text': 'AESM'],
                ['id': 7877, 'value': 'Further Review', 'text': 'Further Review']
        ], 'configs': [
                ['id': 7893, 'value': 'Additional investigations'],
                ['id': 7902, 'value': 'Communication'],
                ['id': 7911, 'value': 'Exchange of Information'],
                ['id': 7884, 'value': 'Meeting'],
                ['id': 7920, 'value': 'Other'],
                ['id': 7929, 'value': 'Periodic Review'],
                ['id': 7938, 'value': 'Review ICSRs']
        ], 'allStatus': [
                ['name': 'Closed', 'value': 'Closed'],
                ['name': 'Deleted', 'value': 'Deleted'],
                ['name': 'InProgress', 'value': 'In Progress'],
                ['name': 'New', 'value': 'New'],
                ['name': 'ReOpened', 'value': 'Re-opened']
        ]]]
        dispositionMap = ['Threshold Not Met': [
                ['displayName': 'Safety Topic', 'abbreviation': 'ST', 'colorCode': '#9A0D0B', 'id': 214, 'validatedConfirmed': false, 'isApprovalRequired': false, 'dispositionClosedStatus': true, 'isReviewed': true],
                ['displayName': 'Validated Signal', 'abbreviation': 'VO', 'colorCode': '#d9534f', 'id': 136, 'validatedConfirmed': true, 'isApprovalRequired': false, 'dispositionClosedStatus': false, 'isReviewed': false],
                ['displayName': 'Requires Review', 'abbreviation': 'RR', 'colorCode': '#5cb85c', 'id': 123, 'validatedConfirmed': false, 'isApprovalRequired': false, 'dispositionClosedStatus': false, 'isReviewed': false],
                ['displayName': 'Continued Monitoring', 'abbreviation': 'CM', 'colorCode': '#337ab7', 'id': 188, 'validatedConfirmed': false, 'isApprovalRequired': false, 'dispositionClosedStatus': false, 'isReviewed': false],
                ['displayName': 'Non-Valid Safety Topic', 'abbreviation': 'NVS', 'colorCode': '#FFFF33', 'id': 253, 'validatedConfirmed': false, 'isApprovalRequired': false, 'dispositionClosedStatus': true, 'isReviewed': true],
                ['displayName': 'Review Completed', 'abbreviation': 'RC', 'colorCode': '#000', 'id': 1754467, 'validatedConfirmed': false, 'isApprovalRequired': false, 'dispositionClosedStatus': true, 'isReviewed': true]
        ], 'Safety Topic': [
                ['displayName': 'Review Next Period', 'abbreviation': 'RNP', 'colorCode': '#9A0D0B', 'id': 240, 'validatedConfirmed': false, 'isApprovalRequired': false, 'dispositionClosedStatus': false, 'isReviewed': false],
                ['displayName': 'Validated Signal', 'abbreviation': 'VO', 'colorCode': '#d9534f', 'id': 136, 'validatedConfirmed': true, 'isApprovalRequired': false, 'dispositionClosedStatus': false, 'isReviewed': false]
        ], 'Validated Signal': [
                ['displayName': 'Requires Review', 'abbreviation': 'RR', 'colorCode': '#5cb85c', 'id': 123, 'validatedConfirmed': false, 'isApprovalRequired': false, 'dispositionClosedStatus': false, 'isReviewed': false],
                ['displayName': 'Confirmed Signal', 'abbreviation': 'CS', 'colorCode': '#d9534f', 'id': 162, 'validatedConfirmed': false, 'isApprovalRequired': false, 'dispositionClosedStatus': true, 'isReviewed': true],
                ['displayName': 'Non-Valid Safety Topic', 'abbreviation': 'NVS', 'colorCode': '#FFFF33', 'id': 253, 'validatedConfirmed': false, 'isApprovalRequired': false, 'dispositionClosedStatus': true, 'isReviewed': true],
                ['displayName': 'Continued Monitoring', 'abbreviation': 'CM', 'colorCode': '#337ab7', 'id': 188, 'validatedConfirmed': false, 'isApprovalRequired': false, 'dispositionClosedStatus': false, 'isReviewed': false]
        ], 'Requires Review': [
                ['displayName': 'Safety Topic', 'abbreviation': 'ST', 'colorCode': '#9A0D0B', 'id': 214, 'validatedConfirmed': false, 'isApprovalRequired': false, 'dispositionClosedStatus': true, 'isReviewed': true],
                ['displayName': 'Refuted Signal', 'abbreviation': 'RS', 'colorCode': '#5cb85c', 'id': 175, 'validatedConfirmed': false, 'isApprovalRequired': false, 'dispositionClosedStatus': false, 'isReviewed': false],
                ['displayName': 'Correction', 'abbreviation': 'COR', 'colorCode': '#5cb85c', 'id': 201, 'validatedConfirmed': false, 'isApprovalRequired': false, 'dispositionClosedStatus': false, 'isReviewed': false],
                ['displayName': 'Non-Valid Observation', 'abbreviation': 'NVO', 'colorCode': '#5cb85c', 'id': 149, 'validatedConfirmed': false, 'isApprovalRequired': false, 'dispositionClosedStatus': true, 'isReviewed': true],
                ['displayName': 'Validated Signal', 'abbreviation': 'VO', 'colorCode': '#d9534f', 'id': 136, 'validatedConfirmed': true, 'isApprovalRequired': false, 'dispositionClosedStatus': false, 'isReviewed': false],
                ['displayName': 'Review Next Period', 'abbreviation': 'RNP', 'colorCode': '#9A0D0B', 'id': 240, 'validatedConfirmed': false, 'isApprovalRequired': false, 'dispositionClosedStatus': false, 'isReviewed': false]
        ], 'Confirmed Signal': [
                ['displayName': 'Safety Topic', 'abbreviation': 'ST', 'colorCode': '#9A0D0B', 'id': 214, 'validatedConfirmed': false, 'isApprovalRequired': false, 'dispositionClosedStatus': true, 'isReviewed': true],
                ['displayName': 'Validated Signal', 'abbreviation': 'VO', 'colorCode': '#d9534f', 'id': 136, 'validatedConfirmed': true, 'isApprovalRequired': false, 'dispositionClosedStatus': false, 'isReviewed': false]
        ], 'Continued Monitoring': [
                ['displayName': 'Refuted Signal', 'abbreviation': 'RS', 'colorCode': '#5cb85c', 'id': 175, 'validatedConfirmed': false, 'isApprovalRequired': false, 'dispositionClosedStatus': false, 'isReviewed': false],
                ['displayName': 'Non-Valid Observation', 'abbreviation': 'NVO', 'colorCode': '#5cb85c', 'id': 149, 'validatedConfirmed': false, 'isApprovalRequired': false, 'dispositionClosedStatus': true, 'isReviewed': true],
                ['displayName': 'Confirmed Signal', 'abbreviation': 'CS', 'colorCode': '#d9534f', 'id': 162, 'validatedConfirmed': false, 'isApprovalRequired': false, 'dispositionClosedStatus': true, 'isReviewed': true]
        ], 'Non-Valid Observation': [
                ['displayName': 'Requires Review', 'abbreviation': 'RR', 'colorCode': '#5cb85c', 'id': 123, 'validatedConfirmed': false, 'isApprovalRequired': false, 'dispositionClosedStatus': false, 'isReviewed': false],
                ['displayName': 'Validated Signal', 'abbreviation': 'VO', 'colorCode': '#d9534f', 'id': 136, 'validatedConfirmed': true, 'isApprovalRequired': false, 'dispositionClosedStatus': false, 'isReviewed': false],
                ['displayName': 'Review Completed', 'abbreviation': 'RC', 'colorCode': '#000', 'id': 1754467, 'validatedConfirmed': false, 'isApprovalRequired': false, 'dispositionClosedStatus': true, 'isReviewed': true]
        ], 'Review Next Period': [
                ['displayName': 'Confirmed Signal', 'abbreviation': 'CS', 'colorCode': '#d9534f', 'id': 162, 'validatedConfirmed': false, 'isApprovalRequired': false, 'dispositionClosedStatus': true, 'isReviewed': true],
                ['displayName': 'Validated Signal', 'abbreviation': 'VO', 'colorCode': '#d9534f', 'id': 136, 'validatedConfirmed': true, 'isApprovalRequired': false, 'dispositionClosedStatus': false, 'isReviewed': false],
                ['displayName': 'Safety Topic', 'abbreviation': 'ST', 'colorCode': '#9A0D0B', 'id': 214, 'validatedConfirmed': false, 'isApprovalRequired': false, 'dispositionClosedStatus': true, 'isReviewed': true]
        ], 'Refuted Signal': [
                ['displayName': 'Non-Valid Observation', 'abbreviation': 'NVO', 'colorCode': '#5cb85c', 'id': 149, 'validatedConfirmed': false, 'isApprovalRequired': false, 'dispositionClosedStatus': true, 'isReviewed': true]
        ]]
        signalNotInAlert = [
                ['name': '13773', 'id': 13701, 'isClosed': false, 'products': '(aj)', 'detectedDate': '16-Jun-2021', 'disposition': 'Validated Signal'],
                ['name': '13773-2', 'id': 17063, 'isClosed': false, 'products': '(FESB COPY With Show ALL , Fesb group copy 4 , APREMILAST, PVAvaccineF)', 'detectedDate': '23-Jun-2021', 'disposition': 'Non-Valid Safety Topic'],
                ['name': '14251-1', 'id': 928630, 'isClosed': false, 'products': '(Test Product d Cream)', 'detectedDate': '06-Aug-2021', 'disposition': 'Validated Signal'],
                ['name': 'aaaaaaaaaaaaaaaaa', 'id': 18227, 'isClosed': false, 'products': '(Test Product AJ)', 'detectedDate': '24-Jun-2021', 'disposition': 'Non-Valid Safety Topic'],
                ['name': 'AJ-Rash', 'id': 2231922, 'isClosed': false, 'products': '(Test Product AJ Capsule 100 millimole)', 'detectedDate': '05-Oct-2021', 'disposition': 'Validated Signal'],
                ['name': 'Apart from counting words and characters, our online editor can help you to improve word choice and writing style, and, optionally, help you to detect grammar mistakes and plagiarism. To check word count, simply place your cursor into the text box above ', 'id': 87787, 'isClosed': false, 'products': '(Ecotrin)', 'detectedDate': '01-Jul-2021', 'disposition': 'Validated Signal'],
                ['name': 'august26', 'id': 1120869, 'isClosed': false, 'products': '(Test Product AJ Capsule 100 millimole)', 'detectedDate': '09-Aug-2021', 'disposition': 'Validated Signal'],
                ['name': 'ddd', 'id': 1273560, 'isClosed': true, 'products': '(Test Product AJ Capsule 100 millimole)', 'detectedDate': '13-Sep-2021', 'disposition': 'Requires Review'],
                ['name': 'dddddddddd', 'id': 16355, 'isClosed': false, 'products': '(Paracetamol, Paracetamol M Tablet, Paracetamol MK ASKU, Paracetamol Tablet 100 mg, TestParaProduct Capsule, para prroduct Capsule, coated, paracetamol Product Cream, paracetamol test Tablet 20 mg, Mycelex Cream Cream 10 mg/dL, Rxlogix Test Poduct 01 Cream 20 mg, Test PVS 2 Cream, Test Product d Cream, Wonder Product Cream, test prod Cream)', 'detectedDate': '21-Jun-2021', 'disposition': 'Non-Valid Safety Topic'],
                ['name': 'dddddddddddddddddd', 'id': 15321, 'isClosed': false, 'products': '(Test Product AJ)', 'detectedDate': '23-Jun-2021', 'disposition': 'Validated Signal'],
                ['name': 'dddsafffffffffffffffffffffs', 'id': 15400, 'isClosed': false, 'products': '(Test Product AJ)', 'detectedDate': '23-Jun-2021', 'disposition': 'Review in Next Period'],
                ['name': 'DD_SIGNAL Count', 'id': 90479, 'isClosed': false, 'products': '(Paracetamol, Paracetamol M Tablet, Paracetamol MK ASKU, Paracetamol Tablet 100 mg, TestParaProduct Capsule, para prroduct Capsule, coated, paracetamol Product Cream, paracetamol test Tablet 20 mg)', 'detectedDate': '02-Jul-2021', 'disposition': 'Validated Signal'],
                ['name': 'eqw', 'id': 26712, 'isClosed': false, 'products': '(Test Product AJ)', 'detectedDate': '25-Jun-2021', 'disposition': 'Refuted Signal'],
                ['name': 'F Signal1', 'id': 1096622, 'isClosed': false, 'products': '(Aj-grp , Test Product AJ Capsule 100 millimole)', 'detectedDate': '20-Aug-2021', 'disposition': 'Safety Topic'],
                ['name': 'FFF Test', 'id': 1089849, 'isClosed': false, 'products': '(Aj-grp , ASPIRIN ALUMINIUM, Wonder Product Cream)', 'detectedDate': '17-Aug-2021', 'disposition': 'Safety Topic'],
                ['name': 'FnaMPmqi1jVuLFJmHAmXogfu0cBYJf0cH9YXs8mEOTgbQU7A7uzUNAZVbf3zwCis6yNwQXCocjOuhyeYJNbQnOpOs2mdF3GCodRRcUEsX58TwO400dq6ogkc6s9cpitU8wdo0ZkEuh8akrwgbmb7r6OfAzJAxXgOb9Rcm8Nb0dSx5jAConaMVhDNRCQaV7vCzbYbGAIEB2naKz3uyqjttSVG1TJcYU94UN3WLrn6TdMJcpLBmQ8zgwPz12HExSO', 'id': 1321483, 'isClosed': false, 'products': '(Test Product d)', 'detectedDate': '17-Sep-2021', 'disposition': 'Validated Signal'],
                ['name': 'FnaMPmqi1jVuLFJmHAmXogfu0cBYJf0cH9YXs8mEOTgbQU7A7uzUNAZVbf3zwCis6yNwQXCocjOuhyeYJNbQnOpOs2mdF3GCodRRcUEsX58TwO400dq6ogkc6s9cpitU8wdo0ZkEuh8akrwgbmb7r6OfAzJAxXgOb9Rcm8Nb0dSx5jAConaMVhDNRCQaV7vCzbYbGAIEB2naKz3uyqjttSVG1TJcYU94UN3WLrn6TdMJcpLBmQ8zgwPz12HExSO', 'id': 1321571, 'isClosed': false, 'products': '(Signal PVS 01)', 'detectedDate': '17-Sep-2021', 'disposition': 'Validated Signal'],
                ['name': 'ggtwPpPRzUgFYkmwAusizpfwZIJPRjdMTpfYPDsPXrVOYvGtpSWCEbXLkjbieFIPlGqFodyiModXHvrRKoAroBlvqlWwntZwiGVbWbqxjEBCdIZsnziaTWzvkwgVBtZnofSECqaCEVMiBzPicBEBIZTEYRdeYQLZALexyuOuuTVdHkAPpzlWDnJHHtBDDPTeVlVoirJKJyrNKHWXVaKRkdJbWLYhErhbgwMwFQmHFZikDgLELYizfUWpOOyFyKy', 'id': 1322273, 'isClosed': false, 'products': '(Test Product AJ)', 'detectedDate': '17-Sep-2021', 'disposition': 'Validated Signal'],
                ['name': 'Gs_TEST_SIgnal_28/07/2021', 'id': 583256, 'isClosed': true, 'products': '(PARACETAMOL)', 'detectedDate': '26-Jul-2021', 'disposition': 'Safety Topic'],
                ['name': 'ISSUE_90001', 'id': 445797, 'isClosed': false, 'products': '', 'detectedDate': '26-Jul-2021', 'disposition': 'Safety Topic']
        ]

        caseDetailMap = ["data": ["Case Information"          : [["Case Number"     : "17JP00000000001411", "Initial Receipt Date": "09-03-17", "Latest followUp Date": "09-03-17",
                                                                      "Report Type"     : "Report From Study", "Country": "JAPAN", "Seriousness": "Serious", "HCP": null,
                                                                      "Reporter Country": null, "Pregnancy": null, "Age Group": null]],
                                      "Product Information"       : [["Product Name": "Calpol", "Family Name": "Japan test family", "Generic Name": "Calpol Study",
                                                                      "Indication"  : null, "Formulation": "Tablet  mg", "Lot Number": null, "Drug Type": "Suspect"]],
                                      "Event Information"         : [["Event PT"            : "Nausea", "Reported Term": "Nausea", "SOC": "Gastrointestinal disorders", "Onset Date": null,
                                                                      "Seriousness Criteria": "-/-/-/-/-/RI/MS", "Event Outcome": null]],
                                      "Product Event Information" : [["Product Name" : "Calpol", "Event PT": "Nausea", "Reported Term": "Nausea", "Core Listedness": "Not Listed",
                                                                      "IB Listedness": "Not Listed", "Reported Causality": null, "Determined Causality": null, "Action Taken": null,
                                                                      "Rechallenge"  : null, Dechallenge: null]],
                                      "Patient Medical History"   : [["Patient Condition Type": null, "Patient Medical Condition PT": null, Notes: null]],
                                      "Cause Of Death Information": [["Cause Of Death Coded": null, "Death Date": null, "Autopsy": "<NULL>", "Autopsy Results": null]],
                                      "Dosage Regimen"            : [["Product Name": "Calpol", "Therapy Start Date": null, "Therapy Stop Date": null, "Therapy Duration": null,
                                                                      "Ongoing"     : null, "Dose": null, "Dose Unit": null, "Daily Dose": null, "lot Number": null, "Expiry Date": null]],
                                      "Lab Results"               : [["Lab Test Name": null, "Test Date": null, "Lab Data Result": null, "Lab Data Result Unit": null, "Normal High": null,
                                                                      "Normal Low"   : null, "Lab Data Assessment": null]],
                                      "Narrative"                 : [["Case Narrative": null, "Case Abbreviated Narrative": null]]], "version": 1,
                             "versionNumberList": [["id": 1, "desc": "Version:1"]],
                             "caseNumber": "17JP00000000001411",
                             "followUpNumber": "-",
                             "ciomsReportUrl": "http://10.100.6.78:9090/reports/report/exportSingleCIOMS?caseNumber=17JP00000000001411&versionNumber=1",
                             "caseDetail": ["alertId"                     : "7267", "priority": "High", "workFlowState": "Communication & Risk Minimization Action",
                                            "caseId":244L, "disposition" : "Threshold Not Met", "isTopicAttached": false, "isValidationStateAchieved": false,
                                            "executedAlertConfigurationId": "7249", "productName": "Calpol", "productFamily": "Japan test family",
                                            "primaryEvent"                : "Nausea", "caseNumber": "17JP00000000001411", "caseVersion": "1", "folowUpNumber": "-", "pt": "Nausea",
                                            "assignedTo"                  : ["id": "9542", "fullName": "Ankit Kumar", "username": "ankit", "email": "ankit.kumar@rxlogix.com"], "execConfigId": "7249"]]

    }

    def cleanup() {
    }

    void "test hasSignalCreationAccessAccess"(){
        given:
        SpringSecurityUtils.metaClass.static.ifAnyGranted = { String role ->
            return true
        }
        when:
        boolean  result=controller.hasSignalCreationAccessAccess()
        then:
        result
    }
    void "test caseDetail"() {
        setup:
        controller.params.caseNumber = "17JP00000000001411"
        controller.params.version = "1"
        controller.params.alertId = "12"
        SpringSecurityUtils.metaClass.static.ifAnyGranted = { String role ->
            return true
        }
        SpringSecurityUtils.metaClass.static.ifAllGranted = { String role ->
            return false
        }
        def userService = Mock(UserService)
        userService.getUser()>>{
            return user
        }
        controller.userService = userService
        def priorityService = Mock(PriorityService)
        priorityService.listPriorityOrder()>>{
            return [[1094665, 'prio 1', 5, null, 90], [7491, 'Urgent', 1, 'mdi mdi-alpha-u-circle red-1', 15], [7502, 'High', 2, 'mdi mdi-alpha-h-circle yellow-1', 30], [7513, 'Medium', 3, 'mdi mdi-alpha-m-circle blue-1', 45], [7524, 'Low', 4, 'mdi mdi-alpha-l-circle green-1', 90], [1257067, 'P1 Priority', 5, 'mdi mdi-alpha-t-circle violet-1', 1]]
        }
        controller.priorityService = priorityService
        controller.validatedSignalService = [fetchSignalsNotInAlertObj: {->
            signalNotInAlert
        } , getActionConfigurationList:{ _ -> return actionConfigurationList}]
        controller.alertService = [isProductSecurity:{ return false},
                                   getActionTypeAndActionMap:{
                                       return actionTypeAndActionMap
                                   }
        ]
        controller.safetyLeadSecurityService =[allAllowedProductsForUser:{_ -> return user.id as Long}]
        workflowRuleService = Mock(WorkflowRuleService)
        workflowRuleService.fetchDispositionIncomingOutgoingMap()>>{
            return dispositionMap
        }
        controller.workflowRuleService = workflowRuleService
        controller.caseInfoService = [getCaseDetailMap: { a, b, c, d, e, f, g, h, i, j, k, l, m -> caseDetailMap},
                                      getFullCaseListData: { _ ->return [version: caseDataDTO?.version, execConfigId: caseDataDTO?.id]},
                                      getGobalAndAlertSpecificCategoriesList : { a,b,c,d,e->
                                                            return ['categoryList':[],'isCategoryEditable':false, 'isFoundAlertArchived':true,'foundAlertId':null] },
                                      fetchCaseVersions:{a,b,c,d,e,f,g ->
                                          return versionsList
                                      }
                                     ]
        when:
        controller.caseDetail("17JP00000000001411","1","5","2",false,false,false,false)
        then:
        noExceptionThrown()
    }

    void "test caseDetail when caseNumber is empty"() {
        setup:
        controller.params.caseNumber = ""
        controller.params.version = "1"
        controller.params.alertId = "1"
        caseDetailMap = ["data": ["Case Information"          : [["Case Number"     : "17JP00000000001411", "Initial Receipt Date": "09-03-17", "Latest followUp Date": "09-03-17",
                                                                      "Report Type"     : "Report From Study", "Country": "JAPAN", "Seriousness": "Serious", "HCP": null,
                                                                      "Reporter Country": null, "Pregnancy": null, "Age Group": null]],
                                      "Product Information"       : [["Product Name": "Calpol", "Family Name": "Japan test family", "Generic Name": "Calpol Study",
                                                                      "Indication"  : null, "Formulation": "Tablet  mg", "Lot Number": null, "Drug Type": "Suspect"]],
                                      "Event Information"         : [["Event PT"            : "Nausea", "Reported Term": "Nausea", "SOC": "Gastrointestinal disorders", "Onset Date": null,
                                                                      "Seriousness Criteria": "-/-/-/-/-/RI/MS", "Event Outcome": null]],
                                      "Product Event Information" : [["Product Name" : "Calpol", "Event PT": "Nausea", "Reported Term": "Nausea", "Core Listedness": "Not Listed",
                                                                      "IB Listedness": "Not Listed", "Reported Causality": null, "Determined Causality": null, "Action Taken": null,
                                                                      "Rechallenge"  : null, Dechallenge: null]],
                                      "Patient Medical History"   : [["Patient Condition Type": null, "Patient Medical Condition PT": null, Notes: null]],
                                      "Cause Of Death Information": [["Cause Of Death Coded": null, "Death Date": null, "Autopsy": "<NULL>", "Autopsy Results": null]],
                                      "Dosage Regimen"            : [["Product Name": "Calpol", "Therapy Start Date": null, "Therapy Stop Date": null, "Therapy Duration": null,
                                                                      "Ongoing"     : null, "Dose": null, "Dose Unit": null, "Daily Dose": null, "lot Number": null, "Expiry Date": null]],
                                      "Lab Results"               : [["Lab Test Name": null, "Test Date": null, "Lab Data Result": null, "Lab Data Result Unit": null, "Normal High": null,
                                                                      "Normal Low"   : null, "Lab Data Assessment": null]],
                                      "Narrative"                 : [["Case Narrative": null, "Case Abbreviated Narrative": null]]], "version": 1,
                             "versionNumberList": [["id": 1, "desc": "Version:1"]],
                             "caseNumber": "17JP00000000001411",
                             "followUpNumber": "-",
                             "ciomsReportUrl": "http://10.100.6.78:9090/reports/report/exportSingleCIOMS?caseNumber=17JP00000000001411&versionNumber=1",
                             "caseDetail": ["alertId"                     : "7267", "priority": "High", "workFlowState": "Communication & Risk Minimization Action",
                                            "disposition"                 : "HCP Education", "isTopicAttached": false, "isValidationStateAchieved": false,
                                            "executedAlertConfigurationId": "7249", "productName": "Calpol", "productFamily": "Japan test family",
                                            "primaryEvent"                : "Nausea", "caseNumber": "17JP00000000001411", "caseVersion": "1", "folowUpNumber": "-", "pt": "Nausea",
                                            "assignedTo"                  : ["id": "9542", "fullName": "Ankit Kumar", "username": "ankit", "email": "ankit.kumar@rxlogix.com"], "execConfigId": "7249"]]

        controller.caseInfoService = [getCaseDetailMap: { a, b, c, d, e ->
            caseDetailMap
        }]
        when:
        controller.caseDetail()
        then:
        model.keySet().size() == 0
        view == "/errors/errorCaseDetail"
    }

    void "test caseDetail when version is empty"() {
        setup:
        controller.params.caseNumber = "17JP00000000001411"
        controller.params.version = ""
        controller.params.alertId = "1"
        def caseDetailMap = ["data": ["Case Information"          : [["Case Number"     : "17JP00000000001411", "Initial Receipt Date": "09-03-17", "Latest followUp Date": "09-03-17",
                                                                      "Report Type"     : "Report From Study", "Country": "JAPAN", "Seriousness": "Serious", "HCP": null,
                                                                      "Reporter Country": null, "Pregnancy": null, "Age Group": null]],
                                      "Product Information"       : [["Product Name": "Calpol", "Family Name": "Japan test family", "Generic Name": "Calpol Study",
                                                                      "Indication"  : null, "Formulation": "Tablet  mg", "Lot Number": null, "Drug Type": "Suspect"]],
                                      "Event Information"         : [["Event PT"            : "Nausea", "Reported Term": "Nausea", "SOC": "Gastrointestinal disorders", "Onset Date": null,
                                                                      "Seriousness Criteria": "-/-/-/-/-/RI/MS", "Event Outcome": null]],
                                      "Product Event Information" : [["Product Name" : "Calpol", "Event PT": "Nausea", "Reported Term": "Nausea", "Core Listedness": "Not Listed",
                                                                      "IB Listedness": "Not Listed", "Reported Causality": null, "Determined Causality": null, "Action Taken": null,
                                                                      "Rechallenge"  : null, Dechallenge: null]],
                                      "Patient Medical History"   : [["Patient Condition Type": null, "Patient Medical Condition PT": null, Notes: null]],
                                      "Cause Of Death Information": [["Cause Of Death Coded": null, "Death Date": null, "Autopsy": "<NULL>", "Autopsy Results": null]],
                                      "Dosage Regimen"            : [["Product Name": "Calpol", "Therapy Start Date": null, "Therapy Stop Date": null, "Therapy Duration": null,
                                                                      "Ongoing"     : null, "Dose": null, "Dose Unit": null, "Daily Dose": null, "lot Number": null, "Expiry Date": null]],
                                      "Lab Results"               : [["Lab Test Name": null, "Test Date": null, "Lab Data Result": null, "Lab Data Result Unit": null, "Normal High": null,
                                                                      "Normal Low"   : null, "Lab Data Assessment": null]],
                                      "Narrative"                 : [["Case Narrative": null, "Case Abbreviated Narrative": null]]], "version": 1,
                             "versionNumberList": [["id": 1, "desc": "Version:1"]],
                             "caseNumber": "17JP00000000001411",
                             "followUpNumber": "-",
                             "ciomsReportUrl": "http://10.100.6.78:9090/reports/report/exportSingleCIOMS?caseNumber=17JP00000000001411&versionNumber=1",
                             "caseDetail": ["alertId"                     : "7267", "priority": "High", "workFlowState": "Communication & Risk Minimization Action",
                                            "disposition"                 : "HCP Education", "isTopicAttached": false, "isValidationStateAchieved": false,
                                            "executedAlertConfigurationId": "7249", "productName": "Calpol", "productFamily": "Japan test family",
                                            "primaryEvent"                : "Nausea", "caseNumber": "17JP00000000001411", "caseVersion": "1", "folowUpNumber": "-", "pt": "Nausea",
                                            "assignedTo"                  : ["id": "9542", "fullName": "Ankit Kumar", "username": "ankit", "email": "ankit.kumar@rxlogix.com"], "execConfigId": "7249"]]

        controller.caseInfoService = [getCaseDetailMap: { a, b, c, d, e ->
            caseDetailMap
        }]
        when:
        controller.caseDetail()
        then:
        model.keySet().size() == 0
        view == "/errors/errorCaseDetail"
    }


    void "test fetchTreeViewNodes"() {
        setup:
        def treeNodes = [["text": "Case Detail", "state": ["opened": true],
                          "children": [[text: "Case Information", "state": ["opened": true], "icon": false, "li_attr": ["id": "case_information"]],
                                       [text: "Product Information", "state": ["opened": true], "icon": false, "li_attr": ["id": "product_information"]],
                                       [text: "Event Information", "state": ["opened": true], "icon": false, li_attr: [id: "event_information"]],
                                       [text: "Product Event Information", state: [opened: true], icon: false, li_attr: [id: "product_event_information"]],
                                       [text: "Patient Medical History", state: [opened: true], icon: false, li_attr: [id: "patient_medical_history"]],
                                       [text: "Cause Of Death Information", state: [opened: true], icon: false, li_attr: [id: "cause_of_death_information"]],
                                       [text: "Dosage Regimen", state: [opened: true], icon: false, li_attr: [id: "dosage_regimen"]],
                                       [text: "Lab Results", state: [opened: true], icon: false, li_attr: [id: "lab_results"]],
                                       [text: "Narrative", state: [opened: true], icon: false, li_attr: [id: "narrative"]]], li_attr: [id: "case_detail"]],
                         [text: "Attachments", state: [opened: true], children: null, li_attr: [id: "attachments"]],
                         [text: "Workflow Management", state: [opened: true], children: null, li_attr: [id: "workflow_management"]],
                         [text: "Comments", state: [opened: true], children: null, li_attr: [id: "comments"]],
                         [text: "Actions", state: [opened: true], children: null, li_attr: [id: "actions"]],
                         [text: "History", state: [opened: true], children: null, li_attr: [id: "history"]]]
        controller.caseInfoService = [getTreeViewNodes:{a,b,c,d,e,f,g,h -> return treeNodes}]

        when:
        controller.fetchTreeViewNodes("1234","WWID1",2L,false,false,false,"No",false)
        then:
        response.getJson()[0]["text"] == "Case Detail"
        response.getJson()[1]["text"] == "Attachments"
        response.getJson()[2]["text"] == "Workflow Management"
    }

    void "test exportCaseInfo"(){
        given:
        String caseNumber = "171000S"
        Long alertConfigId = 2L
        String version = ""
        String followUpNumber = ""
        Long alertId = 1L
        Long exeConfigId = 3L
        params.outputFormat ="pdf"
        controller.caseHistoryService = [listCaseHistory:{a,b,c,d ->return []},
                                         getDefaultHistoryMap: {->return [['alertName':null, 'disposition':null, 'justification':null, 'priority':null, 'alertTags':null, 'updatedBy':null, 'timestamp':null]]
                                         },
                                         listSuspectCaseHistory:{a,b ->return []},
                                        ]
        controller.caseInfoService = [getCaseInfoMap:{a,b,c,d,e,f,g,h,i -> return caseInfoMap}, hasValues:{ a -> return caseInfoMapValues},
                                      getAttachmentListMap:{a -> return [['name':'', 'description':'', 'timeStamp':'', 'modifiedBy':'']]},

        ]
        controller.alertCommentService = [getUpdatedCommentMap:{a,b,c->return [['comments':'']]}]
        controller.actionService =[getActionListMap:{a-> return[['id':null, 'type':'', 'action':'', 'details':'', 'dueDate':'', 'assignedTo':'', 'status':'', 'completionDate':'']]}]
        def dynamicReportService = Mock(DynamicReportService)
        dynamicReportService.getContentType(ReportFormat.PDF)
        dynamicReportService.createCaseDetailReport(_,_,_,_,_,_,_)>> { return file}
        controller.dynamicReportService = dynamicReportService
        when:
        controller.exportCaseInfo(false,false,caseNumber,alertConfigId,"Test Product Family AJ",version,followUpNumber,alertId,false,"",false,exeConfigId)
        then:
        noExceptionThrown()
    }
    void "test fetchTreeViewNodes if service returns empty keys"() {
        given:
        def treeNodes = []
        controller.caseInfoService = [getTreeViewNodes:{a,b,c,d,e,f,g,h -> return treeNodes}]
        when:
        controller.fetchTreeViewNodes("1234","WWID1",2L,false,false,false,"No",false)
        then:
        response.getJson() == []


    }

    void "test evdasCaseDetail with case number"() {
        setup:
        controller.params.caseNumber = "17JP00000000001411"
        controller.params.version = "1"
        controller.params.alertId = "1"
        controller.params.wwid = null
        def caseDetailMap = ["data": ["Case Information"          : [["Case Number"     : "17JP00000000001411", "Initial Receipt Date": "09-03-17", "Latest followUp Date": "09-03-17",
                                                                      "Report Type"     : "Report From Study", "Country": "JAPAN", "Seriousness": "Serious", "HCP": null,
                                                                      "Reporter Country": null, "Pregnancy": null, "Age Group": null]],
                                      "Product Information"       : [["Product Name": "Calpol", "Family Name": "Japan test family", "Generic Name": "Calpol Study",
                                                                      "Indication"  : null, "Formulation": "Tablet  mg", "Lot Number": null, "Drug Type": "Suspect"]],
                                      "Event Information"         : [["Event PT"            : "Nausea", "Reported Term": "Nausea", "SOC": "Gastrointestinal disorders", "Onset Date": null,
                                                                      "Seriousness Criteria": "-/-/-/-/-/RI/MS", "Event Outcome": null]],
                                      "Product Event Information" : [["Product Name" : "Calpol", "Event PT": "Nausea", "Reported Term": "Nausea", "Core Listedness": "Not Listed",
                                                                      "IB Listedness": "Not Listed", "Reported Causality": null, "Determined Causality": null, "Action Taken": null,
                                                                      "Rechallenge"  : null, Dechallenge: null]],
                                      "Patient Medical History"   : [["Patient Condition Type": null, "Patient Medical Condition PT": null, Notes: null]],
                                      "Cause Of Death Information": [["Cause Of Death Coded": null, "Death Date": null, "Autopsy": "<NULL>", "Autopsy Results": null]],
                                      "Dosage Regimen"            : [["Product Name": "Calpol", "Therapy Start Date": null, "Therapy Stop Date": null, "Therapy Duration": null,
                                                                      "Ongoing"     : null, "Dose": null, "Dose Unit": null, "Daily Dose": null, "lot Number": null, "Expiry Date": null]],
                                      "Lab Results"               : [["Lab Test Name": null, "Test Date": null, "Lab Data Result": null, "Lab Data Result Unit": null, "Normal High": null,
                                                                      "Normal Low"   : null, "Lab Data Assessment": null]],
                                      "Narrative"                 : [["Case Narrative": null, "Case Abbreviated Narrative": null]]], "version": 1,
                             "versionNumberList": [["id": 1, "desc": "Version:1"]],
                             "caseNumber": "17JP00000000001411",
                             "followUpNumber": "-",
                             "ciomsReportUrl": "http://10.100.6.78:9090/reports/report/exportSingleCIOMS?caseNumber=17JP00000000001411&versionNumber=1",
                             "caseDetail": ["alertId"                     : "7267", "priority": "High", "workFlowState": "Communication & Risk Minimization Action",
                                            "disposition"                 : "HCP Education", "isTopicAttached": false, "isValidationStateAchieved": false,
                                            "executedAlertConfigurationId": "7249", "productName": "Calpol", "productFamily": "Japan test family",
                                            "primaryEvent"                : "Nausea", "caseNumber": "17JP00000000001411", "caseVersion": "1", "folowUpNumber": "-", "pt": "Nausea",
                                            "assignedTo"                  : ["id": "9542", "fullName": "Ankit Kumar", "username": "ankit", "email": "ankit.kumar@rxlogix.com"], "execConfigId": "7249"]]

        def caseInfoServiceMocked = Mock(CaseInfoService)
        caseInfoServiceMocked.getEvdasCaseDetailMap(_, _, _, _) >> caseDetailMap
        caseInfoServiceMocked.getEvdasCaseDetailMap(_, _) >> caseDetailMap
        controller.caseInfoService = caseInfoServiceMocked

        when:
        controller.evdasCaseDetail()

        then:
        0 * controller.caseInfoService.getEvdasCaseDetailMap(_, _)
        1 * controller.caseInfoService.getEvdasCaseDetailMap(_, _, _, _)
    }

    void "test evdasCaseDetail with WWID"() {
        setup:
        controller.params.caseNumber = null
        controller.params.version = null
        controller.params.alertId = "1"
        controller.params.wwid = "WWID123456"
        def caseDetailMap = ["data": ["Case Information"          : [["Case Number"     : "17JP00000000001411", "Initial Receipt Date": "09-03-17", "Latest followUp Date": "09-03-17",
                                                                      "Report Type"     : "Report From Study", "Country": "JAPAN", "Seriousness": "Serious", "HCP": null,
                                                                      "Reporter Country": null, "Pregnancy": null, "Age Group": null]],
                                      "Product Information"       : [["Product Name": "Calpol", "Family Name": "Japan test family", "Generic Name": "Calpol Study",
                                                                      "Indication"  : null, "Formulation": "Tablet  mg", "Lot Number": null, "Drug Type": "Suspect"]],
                                      "Event Information"         : [["Event PT"            : "Nausea", "Reported Term": "Nausea", "SOC": "Gastrointestinal disorders", "Onset Date": null,
                                                                      "Seriousness Criteria": "-/-/-/-/-/RI/MS", "Event Outcome": null]],
                                      "Product Event Information" : [["Product Name" : "Calpol", "Event PT": "Nausea", "Reported Term": "Nausea", "Core Listedness": "Not Listed",
                                                                      "IB Listedness": "Not Listed", "Reported Causality": null, "Determined Causality": null, "Action Taken": null,
                                                                      "Rechallenge"  : null, Dechallenge: null]],
                                      "Patient Medical History"   : [["Patient Condition Type": null, "Patient Medical Condition PT": null, Notes: null]],
                                      "Cause Of Death Information": [["Cause Of Death Coded": null, "Death Date": null, "Autopsy": "<NULL>", "Autopsy Results": null]],
                                      "Dosage Regimen"            : [["Product Name": "Calpol", "Therapy Start Date": null, "Therapy Stop Date": null, "Therapy Duration": null,
                                                                      "Ongoing"     : null, "Dose": null, "Dose Unit": null, "Daily Dose": null, "lot Number": null, "Expiry Date": null]],
                                      "Lab Results"               : [["Lab Test Name": null, "Test Date": null, "Lab Data Result": null, "Lab Data Result Unit": null, "Normal High": null,
                                                                      "Normal Low"   : null, "Lab Data Assessment": null]],
                                      "Narrative"                 : [["Case Narrative": null, "Case Abbreviated Narrative": null]]], "version": 1,
                             "versionNumberList": [["id": 1, "desc": "Version:1"]],
                             "caseNumber": "17JP00000000001411",
                             "followUpNumber": "-",
                             "ciomsReportUrl": "http://10.100.6.78:9090/reports/report/exportSingleCIOMS?caseNumber=17JP00000000001411&versionNumber=1",
                             "caseDetail": ["alertId"                     : "7267", "priority": "High", "workFlowState": "Communication & Risk Minimization Action",
                                            "disposition"                 : "HCP Education", "isTopicAttached": false, "isValidationStateAchieved": false,
                                            "executedAlertConfigurationId": "7249", "productName": "Calpol", "productFamily": "Japan test family",
                                            "primaryEvent"                : "Nausea", "caseNumber": "17JP00000000001411", "caseVersion": "1", "folowUpNumber": "-", "pt": "Nausea",
                                            "assignedTo"                  : ["id": "9542", "fullName": "Ankit Kumar", "username": "ankit", "email": "ankit.kumar@rxlogix.com"], "execConfigId": "7249"]]

        def caseInfoServiceMocked = Mock(CaseInfoService)
        caseInfoServiceMocked.getEvdasCaseDetailMap(_, _, _, _) >> caseDetailMap
        caseInfoServiceMocked.getEvdasCaseDetailMap(_, _) >> caseDetailMap
        controller.caseInfoService = caseInfoServiceMocked

        when:
        controller.evdasCaseDetail()

        then:
        1 * controller.caseInfoService.getEvdasCaseDetailMap(_, _)
        0 * controller.caseInfoService.getEvdasCaseDetailMap(_, _, _, _)
    }

    void "test evdasCaseDetail without case number and WWID"() {
        setup:
        controller.params.caseNumber = null
        controller.params.version = "1"
        controller.params.alertId = "1"
        controller.params.wwid = null
        def caseDetailMap = ["data": ["Case Information"          : [["Case Number"     : "17JP00000000001411", "Initial Receipt Date": "09-03-17", "Latest followUp Date": "09-03-17",
                                                                      "Report Type"     : "Report From Study", "Country": "JAPAN", "Seriousness": "Serious", "HCP": null,
                                                                      "Reporter Country": null, "Pregnancy": null, "Age Group": null]],
                                      "Product Information"       : [["Product Name": "Calpol", "Family Name": "Japan test family", "Generic Name": "Calpol Study",
                                                                      "Indication"  : null, "Formulation": "Tablet  mg", "Lot Number": null, "Drug Type": "Suspect"]],
                                      "Event Information"         : [["Event PT"            : "Nausea", "Reported Term": "Nausea", "SOC": "Gastrointestinal disorders", "Onset Date": null,
                                                                      "Seriousness Criteria": "-/-/-/-/-/RI/MS", "Event Outcome": null]],
                                      "Product Event Information" : [["Product Name" : "Calpol", "Event PT": "Nausea", "Reported Term": "Nausea", "Core Listedness": "Not Listed",
                                                                      "IB Listedness": "Not Listed", "Reported Causality": null, "Determined Causality": null, "Action Taken": null,
                                                                      "Rechallenge"  : null, Dechallenge: null]],
                                      "Patient Medical History"   : [["Patient Condition Type": null, "Patient Medical Condition PT": null, Notes: null]],
                                      "Cause Of Death Information": [["Cause Of Death Coded": null, "Death Date": null, "Autopsy": "<NULL>", "Autopsy Results": null]],
                                      "Dosage Regimen"            : [["Product Name": "Calpol", "Therapy Start Date": null, "Therapy Stop Date": null, "Therapy Duration": null,
                                                                      "Ongoing"     : null, "Dose": null, "Dose Unit": null, "Daily Dose": null, "lot Number": null, "Expiry Date": null]],
                                      "Lab Results"               : [["Lab Test Name": null, "Test Date": null, "Lab Data Result": null, "Lab Data Result Unit": null, "Normal High": null,
                                                                      "Normal Low"   : null, "Lab Data Assessment": null]],
                                      "Narrative"                 : [["Case Narrative": null, "Case Abbreviated Narrative": null]]], "version": 1,
                             "versionNumberList": [["id": 1, "desc": "Version:1"]],
                             "caseNumber": "17JP00000000001411",
                             "followUpNumber": "-",
                             "ciomsReportUrl": "http://10.100.6.78:9090/reports/report/exportSingleCIOMS?caseNumber=17JP00000000001411&versionNumber=1",
                             "caseDetail": ["alertId"                     : "7267", "priority": "High", "workFlowState": "Communication & Risk Minimization Action",
                                            "disposition"                 : "HCP Education", "isTopicAttached": false, "isValidationStateAchieved": false,
                                            "executedAlertConfigurationId": "7249", "productName": "Calpol", "productFamily": "Japan test family",
                                            "primaryEvent"                : "Nausea", "caseNumber": "17JP00000000001411", "caseVersion": "1", "folowUpNumber": "-", "pt": "Nausea",
                                            "assignedTo"                  : ["id": "9542", "fullName": "Ankit Kumar", "username": "ankit", "email": "ankit.kumar@rxlogix.com"], "execConfigId": "7249"]]

        def caseInfoServiceMocked = Mock(CaseInfoService)
        caseInfoServiceMocked.getEvdasCaseDetailMap(_, _, _, _) >> caseDetailMap
        caseInfoServiceMocked.getEvdasCaseDetailMap(_, _) >> caseDetailMap
        controller.caseInfoService = caseInfoServiceMocked

        when:
        controller.evdasCaseDetail()

        then:
        0 * controller.caseInfoService.getEvdasCaseDetailMap(_, _)
        0 * controller.caseInfoService.getEvdasCaseDetailMap(_, _, _, _)
    }

    void "test fetchVersionsCaseDetail"() {
        setup:
        controller.params.caseNumber = null
        controller.params.version = "1"
        controller.params.alertId = "1"
        def caseDetailMap = ["data": ["Case Information"          : [["Case Number"     : "17JP00000000001411", "Initial Receipt Date": "09-03-17", "Latest followUp Date": "09-03-17",
                                                                       "Report Type"     : "Report From Study", "Country": "JAPAN", "Seriousness": "Serious", "HCP": null,
                                                                       "Reporter Country": null, "Pregnancy": null, "Age Group": null]],
                                       "Product Information"       : [["Product Name": "Calpol", "Family Name": "Japan test family", "Generic Name": "Calpol Study",
                                                                       "Indication"  : null, "Formulation": "Tablet  mg", "Lot Number": null, "Drug Type": "Suspect"]],
                                       "Event Information"         : [["Event PT"            : "Nausea", "Reported Term": "Nausea", "SOC": "Gastrointestinal disorders", "Onset Date": null,
                                                                       "Seriousness Criteria": "-/-/-/-/-/RI/MS", "Event Outcome": null]],
                                       "Product Event Information" : [["Product Name" : "Calpol", "Event PT": "Nausea", "Reported Term": "Nausea", "Core Listedness": "Not Listed",
                                                                       "IB Listedness": "Not Listed", "Reported Causality": null, "Determined Causality": null, "Action Taken": null,
                                                                       "Rechallenge"  : null, Dechallenge: null]],
                                       "Patient Medical History"   : [["Patient Condition Type": null, "Patient Medical Condition PT": null, Notes: null]],
                                       "Cause Of Death Information": [["Cause Of Death Coded": null, "Death Date": null, "Autopsy": "<NULL>", "Autopsy Results": null]],
                                       "Dosage Regimen"            : [["Product Name": "Calpol", "Therapy Start Date": null, "Therapy Stop Date": null, "Therapy Duration": null,
                                                                       "Ongoing"     : null, "Dose": null, "Dose Unit": null, "Daily Dose": null, "lot Number": null, "Expiry Date": null]],
                                       "Lab Results"               : [["Lab Test Name": null, "Test Date": null, "Lab Data Result": null, "Lab Data Result Unit": null, "Normal High": null,
                                                                       "Normal Low"   : null, "Lab Data Assessment": null]],
                                       "Narrative"                 : [["Case Narrative": null, "Case Abbreviated Narrative": null]]], "version": 1,
                              "absentValue": "Lab Results,Narrative,Cause Of Death Information,Patient Medical History"]


        controller.caseInfoService = [getCaseDetailMap: { a, b, c, d, e , f, g, h, j, k, l, m->
            caseDetailMap
        }]

        when:
        controller.fetchVersionsCaseDetail()

        then:
        response.getJson()["versionAvailableData"] == caseDetailMap.data
        response.getJson()["versionCompareData"] == caseDetailMap.data
        response.getJson()["absentValues"] == "Lab Results,Narrative,Cause Of Death Information,Patient Medical History"
    }

    void "test compareVersions"() {
        setup:
        controller.params.isArchived = false
        controller.params.isAdhocRun = false
        controller.params.caseNumber = "17JP00000000001411"
        controller.params.followUpNumber = "0"
        controller.params.versionAvailable = "1"
        controller.params.versionCompare = "2"
        controller.params.followUpAvailable = "0"
        controller.params.followUpCompare = "1"
        controller.params.isSingleAlertScreen = false
        controller.params.isArgusDataSource = true

        def caseInfoServiceMocked = Mock(CaseInfoService)
        caseInfoServiceMocked.getAlertDetailMap(_, _, _, _,_) >> [:]
        caseInfoServiceMocked.compareCategories(_, _) >> []
        caseInfoServiceMocked.fetchCaseVersions(_, _,_,_,_,_,_) >> [:]
        caseInfoServiceMocked.getGobalAndAlertSpecificCategoriesList(_,_,_,_) >> [:]
        controller.caseInfoService = caseInfoServiceMocked



        when:
        controller.compareVersions()

        then:
        model["versionCompare"] == "1"
        model["versionAvailable"] == "2"
    }

    void "test hasSingleReviewerAccess when access is given"(){
        given:
        SpringSecurityUtils.metaClass.static.ifAnyGranted = { String role ->
            return true
        }
        when:
        boolean  result=controller.hasSingleReviewerAccess()
        then:
        result
    }

    void "test hasSingleReviewerAccess when access is not given"(){
        given:
        SpringSecurityUtils.metaClass.static.ifAnyGranted = { String role ->
            return false
        }
        when:
        boolean  result=controller.hasSingleReviewerAccess()
        then:
        result == false
    }

    void "test fetchOutcomeForVAERS"(){
        when:
        grailsApplication.config.pvsignal.caseDetail.field.outcome.vaers = ['DI': 'Died', 'LT': 'Life Threatening', 'ER': 'Emergency Room', 'HO': 'Hospitalized', 'XS': 'Prolonged Hospitalization',
                                                                            'DS': 'DISABLE', 'BD': 'Congenital Anomaly or Birth Defect', 'CV': 'Doctor or other healthcare professional office/clinic visit', 'UC': 'Emergency room/department or urgent care']

        controller.params.abbreviatedOutcome = 'LT'
        controller.fetchOutcomeForVAERS()
        then:
        noExceptionThrown()
    }

    void "test caseDiff"(){
        given:
        controller.caseInfoService = [caseDifference:{a,b -> return [
                "genInfo"         : ['followUpDate':'26-Nov-2021','caseLockedDate':'26-Nov-2021' ],
                "prodInfo"        : [:],
                "eventInfo"       : [:],
                "modifiedInfo"    : [:],
                "deletedEventInfo": [:],
                "deletedProdInfo" : [:]
        ]  }]
        when:
        controller.params.caseNumber = "CASE101"
        controller.params.followUpNumber = ""
        controller.caseDiff()
        then:
        noExceptionThrown()
    }

    void "test renderReportOutputType"(){
        given:
        params.outputFormat ="pdf"
        def dynamicReportService = Mock(DynamicReportService)
        dynamicReportService.getContentType(ReportFormat.PDF)
        controller.dynamicReportService = dynamicReportService
        when:
        controller.renderReportOutputType(file)
        then:
        noExceptionThrown()
    }

    void "test prepareCaseDataDTO"(){
        given:
        params.alertId = 294112
        params.execConfigId = 2425
        params.totalCount = 6
        when:
        controller.prepareCaseDataDTO()
        then:
        CaseDataDTO
    }

    void "test getCaseNarativeInfo"(){
        given:
        controller.caseInfoService = [getCaseNarativeInfo:{a,b -> return ['newNarrative':'Narratives for Test Product AjJand Fever.', 'oldNarrative':'Narratives for Test Product AjJand Fever.'] }]
        when:
        controller.getCaseNarativeInfo()
        then:
        noExceptionThrown()
    }


}
