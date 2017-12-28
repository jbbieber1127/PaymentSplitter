import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;


public class PaymentSplitterController {

    private final int ACCOUNT_NAME_MAX_LENGTH = 15;
    private final int PAYMENT_NAME_MAX_LENGTH = 20;

    // The account that is currently selected in the list view. Default to the first one.
    private Account selectedAccount;
    private Payment selectedPayment;
    private Account selectedUninvolvedAccount;
    private Account selectedInvolvedAccount;
    private Account selectedAccountTotal;

    private ArrayList<Account> accounts;
    private ArrayList<Payment> payments;
    private ArrayList<Account> uninvolvedAccounts;
    private HashMap<HBox, Account> involvedAccounts = new HashMap<>();

    private int accountCount;
    private int paymentCount;

    private int initializedTracker = 0;

    // Accounts Tab
    @FXML
    private AnchorPane errorPane;
    @FXML
    private Label errorLabel;
    @FXML
    private TextField selectedAccountNameField;
    @FXML
    private ListView accountsListView;
    @FXML
    private Button deleteAccountButton;
    // Payments Tab
    @FXML
    private TextField selectedPaymentNameField;
    @FXML
    private ListView paymentsListView;
    @FXML
    private ListView uninvolvedAccountsListView;
    @FXML
    private ListView involvedAccountsListView;
    @FXML
    private ListView involvedPaymentsList;
    @FXML
    private Label maxAccountNameErrorLabel;
    @FXML
    private Label maxPaymentNameErrorLabel;
    // Totals Tab
    @FXML
    private TableView<Account> accountTotalsTableView;
    @FXML
    private TableColumn<Account, String> accountTableColumn;
    @FXML
    private TableColumn<Account, Double> payedTableColumn;
    @FXML
    private TableColumn<Account, Double> owedTableColumn;
    // Save/Load Tab
    @FXML
    private TextField loadPathField;
    @FXML
    private TextField savePathField;
    @FXML
    private Text filePathErrorText;

    @FXML
    private void initialize(){
        accountCount = 1;
        accounts = new ArrayList<>();
        accountsListView.getItems().addAll(accounts);
        paymentCount = 1;
        payments = new ArrayList<>();
        paymentsListView.getItems().addAll(payments);
        uninvolvedAccounts = new ArrayList<>();
        errorPane.setVisible(false);
        addTextLimiter(selectedAccountNameField, ACCOUNT_NAME_MAX_LENGTH);
        addTextLimiter(selectedPaymentNameField, PAYMENT_NAME_MAX_LENGTH);
        maxAccountNameErrorLabel.setText("(Max length: " + ACCOUNT_NAME_MAX_LENGTH + " characters)");
        maxAccountNameErrorLabel.setVisible(false);
        maxPaymentNameErrorLabel.setText("(Max length: " + PAYMENT_NAME_MAX_LENGTH + " characters)");
        maxPaymentNameErrorLabel.setVisible(false);
        accountTableColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        payedTableColumn.setCellValueFactory(new PropertyValueFactory<>("totalPayed"));
        owedTableColumn.setCellValueFactory(new PropertyValueFactory<>("totalOwed"));
        filePathErrorText.setVisible(false);
    }

    public static void addTextLimiter(final TextField tf, final int maxLength) {
        tf.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(final ObservableValue<? extends String> ov, final String oldValue, final String newValue) {
                if (tf.getText().length() > maxLength) {
                    String s = tf.getText().substring(0, maxLength);
                    tf.setText(s);
                }
                String t = tf.getText();
                while(t != t.replace(',', (char) 0)){
                    t = t.replace(',', (char) 0);
                }
                tf.setText(t);
            }
        });
    }

    @FXML
    private void saveButtonClicked(){
        if(!(accounts.size() == 0 && payments.size() == 0) && savePathField.getText().length() > 0) {
            HashMap<Account, Integer> ids = new HashMap<>();
            String write = "accounts\r\n";
            for (int i = 0; i < accounts.size(); i++) {
                ids.put(accounts.get(i), i);
                String thisLine = "";

                // Write the id then the account name
                thisLine += i + "," + accounts.get(i).getName();
                thisLine += "\r\n";

                write += thisLine;
            }
            write += "payments\r\n";
            for (int i = 0; i < payments.size(); i++) {
                String thisLine = "";
                thisLine += payments.get(i).getName() + ",";
                ArrayList<Account> accs = payments.get(i).getAccounts();
                for (int j = 0; j < accs.size(); j++) {
                    // Write the ID then the amount payed
                    thisLine += ids.get(accs.get(j)) + "," + payments.get(i).getAmountPayed(accs.get(j)) + ",";
                }
                thisLine += "\r\n";

                write += thisLine;
            }
            try {
                PrintWriter out = new PrintWriter(savePathField.getText() + ".txt");
                out.println(write);
                out.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void loadButtonClicked(){
        if(loadPathField.getText().length() > 0){
            ArrayList<Account> newAccounts = new ArrayList<>();
            ArrayList<Payment> newPayments = new ArrayList<>();
            String file = readFile(loadPathField.getText() + ".txt");
            if(!file.equals("")) {
                String[] lines = file.split("\r\n");
                boolean isAccounts = false;
                HashMap<Integer, Account> accs = new HashMap<>();
                for (int i = 0; i < lines.length; i++) {
                    String[] thisLine = lines[i].split(",");

                    if (lines[i].equals("accounts")) {
                        isAccounts = true;
                    } else if (lines[i].equals("payments")) {
                        isAccounts = false;
                    } else {
                        Payment currentPayment = new Payment();
                        boolean prevWasInt = false;
                        int key = -1;
                        for (int j = 0; j < thisLine.length; j++) {
                            if (isAccounts) {
                                try {
                                    key = Integer.parseInt(thisLine[j]);
                                    prevWasInt = true;
                                } catch (Exception e) {
                                    if (prevWasInt) {
                                        prevWasInt = false;
                                        Account a = new Account();
                                        a.setName(thisLine[j]);
                                        accs.put(key, a);
                                        newAccounts.add(a);
                                    }
                                }
                            } else {
                                if (j == 0) {
                                    currentPayment.setName(thisLine[j]);
                                    newPayments.add(currentPayment);
                                } else {
                                    if (j % 2 == 1) {
                                        currentPayment.getAccounts().add(accs.get(Integer.parseInt(thisLine[j])));
                                    } else {
                                        currentPayment.setAmountPayed(currentPayment.getAccounts().get(currentPayment.getAccounts().size() - 1), Double.parseDouble(thisLine[j]));
                                    }
                                }
                            }
                        }
                    }
                }
                addAccounts(newAccounts);
                addPayments(newPayments);
            }
        }
    }

    private void addAccounts(ArrayList<Account> newAccounts){
        for(int i = 0; i < newAccounts.size(); i++){
            accounts.add(newAccounts.get(i));
            accountsListView.getItems().add(newAccounts.get(i));
            accountTotalsTableView.getItems().add(newAccounts.get(i));
            // Prevents the horizontal scroll bar from appearing
            if(accounts.size() >= 16){
                owedTableColumn.setPrefWidth(62);
            }else{
                owedTableColumn.setPrefWidth(75);
            }
            accountsListView.getSelectionModel().select(accounts.size() - 1);
        }
        accountsListViewActionPerformed();
    }

    private void addPayments(ArrayList<Payment> newPayments){
        for (int i = 0; i < newPayments.size(); i++) {
            payments.add(newPayments.get(i));
            paymentsListView.getItems().add(newPayments.get(i));
            paymentsListView.getSelectionModel().select(payments.size() - 1);
        }
        // Just to update everything
        updateTotals();
        tabPaneSelectionChanged();
    }

    private static String readFile(String path) {
        byte[] encoded = {};
        try {
            encoded = Files.readAllBytes(Paths.get(path));
        }catch(Exception e){

        }
        return new String(encoded);
    }

    @FXML
    private void accountTotalsTableViewActionPerformed(){
        selectedAccountTotal = accountTotalsTableView.getSelectionModel().getSelectedItem();
        if(selectedAccountTotal != null){

        }else{

        }
    }

    @FXML
    private void tabPaneSelectionChanged(){
        // Avoids exceptions on init
        if(initializedTracker > 1) {
            uninvolvedAccountsListViewActionPerformed();
            involvedAccountsListViewActionPerformed();
            accountsListViewActionPerformed();
            paymentListViewActionPerformed();
            accountTotalsTableViewActionPerformed();
            accountTotalsTableView.refresh();
        }else{
            initializedTracker++;
        }
    }

    @FXML
    private void uninvolvedAccountsListViewActionPerformed(){
        selectedUninvolvedAccount = (Account) uninvolvedAccountsListView.getSelectionModel().getSelectedItem();
        if(selectedUninvolvedAccount != null) {
            uninvolvedAccountsListView.scrollTo(selectedUninvolvedAccount);
        }else{

        }
    }

    @FXML
    private void involvedAccountsListViewActionPerformed(){
        selectedInvolvedAccount = involvedAccounts.get(involvedAccountsListView.getSelectionModel().getSelectedItem());
        if(selectedInvolvedAccount != null) {
            involvedAccountsListView.scrollTo(selectedInvolvedAccount);
        }else{

        }
    }

    @FXML
    private void involveAccountButtonClicked(){
        if(selectedUninvolvedAccount != null) {
            int selectNext = uninvolvedAccountsListView.getItems().indexOf(selectedUninvolvedAccount);
            if(selectNext == uninvolvedAccountsListView.getItems().size() - 1){
                selectNext = uninvolvedAccountsListView.getItems().size() - 2;
            }
            uninvolvedAccountsListView.getItems().remove(selectedUninvolvedAccount);
            uninvolvedAccountsListView.getSelectionModel().select(selectNext);

            // Add the item
            displayInvolvedAccount(selectedUninvolvedAccount);

            uninvolvedAccountsListViewActionPerformed();
            involvedAccountsListViewActionPerformed();
        }
    }

    @FXML
    private void involveAllButtonClicked(){
        for(Account a : accounts){
            if(!selectedPayment.getAccounts().contains(a)) {
                uninvolvedAccountsListView.getItems().remove(a);
                displayInvolvedAccount(a);
            }
        }
        uninvolvedAccountsListViewActionPerformed();
        involvedAccountsListViewActionPerformed();
    }

    private void displayInvolvedAccount(Account act){
        for(int i = 0; i < involvedAccounts.keySet().size(); i++){
            if(involvedAccounts.get(involvedAccounts.keySet().toArray()[i]).equals(act)){
                HBox tgt = (HBox) involvedAccounts.keySet().toArray()[i];
                involvedAccounts.remove(tgt);
            }
        }

        HBox hbox = new HBox();
        involvedAccounts.put(hbox, act);
        hbox.setAlignment(Pos.CENTER_RIGHT);

        Label l = new Label(act.toString());
        l.setMaxWidth(120);
        hbox.getChildren().add(l);

        Separator sep = new Separator();
        sep.setVisible(false);
        hbox.getChildren().add(sep);

        TextField accountPayedField = new TextField();
        accountPayedField.setPrefWidth(55);
        String amount = "" + selectedPayment.getAmountPayed(act);
        if(amount.endsWith(".0")){
            amount = amount.substring(0, amount.length() - 2);
        }
        accountPayedField.setText(amount);
        hbox.getChildren().add(accountPayedField);

        accountPayedField.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue,
                                String newValue) {
                if (!newValue.matches("-\\d*\\.\\d*")) {
                    newValue = newValue.replaceAll("[^-\\d\\.\\d]", "");
                    accountPayedField.setText(newValue);
                }
                if(newValue.lastIndexOf('.') != newValue.indexOf('.')){
                    boolean firstPeriod = false;
                    String tmp = "";
                    for(int i = 0; i < newValue.length(); i++){
                        if(newValue.charAt(i) == '.'){
                            if(!firstPeriod){
                                firstPeriod = true;
                                tmp += newValue.charAt(i);
                            }
                        }else{
                            tmp += newValue.charAt(i);
                        }
                    }
                    newValue = tmp;
                    accountPayedField.setText(newValue);
                }
                if(newValue.lastIndexOf('-') != newValue.indexOf('-')){
                    boolean firstPeriod = false;
                    String tmp = "";
                    for(int i = 0; i < newValue.length(); i++){
                        if(newValue.charAt(i) == '-'){
                            if(!firstPeriod){
                                firstPeriod = true;
                                tmp += newValue.charAt(i);
                            }
                        }else{
                            tmp += newValue.charAt(i);
                        }
                    }
                    newValue = tmp;
                    accountPayedField.setText(newValue);
                }
                if(newValue.indexOf('-') != 0){
                    newValue = newValue.replace('-', (char) 0);
                }
                selectedPayment.setAmountPayed(involvedAccounts.get(hbox), newValue.length() == 0 || newValue.equals(".") || newValue.equals("-") || newValue.equals("-.") ? 0 : Double.parseDouble(newValue));
                updateTotals();
                accountTotalsTableView.refresh();
            }
        });
        involvedAccountsListView.getItems().add(hbox);
        if(!selectedPayment.getAccounts().contains(act)) {
            selectedPayment.getAccounts().add(act);
        }
        involvedAccountsListView.getSelectionModel().select(selectedPayment.getAccounts().size() - 1);
    }

    private void updateTotals(){
        HashMap<Account, Double> payed = new HashMap<>();
        HashMap<Account, Double> owes = new HashMap<>();
        for(Account a : accounts){
            payed.put(a, 0.);
            owes.put(a, 0.);
        }
        for(int i = 0; i < payments.size(); i++){
            ArrayList<Account> accs = payments.get(i).getAccounts();
            double totalThisPayment = 0;
            for(int j = 0; j < accs.size(); j++){
                totalThisPayment += payments.get(i).getAmountPayed(accs.get(j));
                payed.put(accs.get(j), payed.get(accs.get(j)) + payments.get(i).getAmountPayed(accs.get(j)));
            }
            for(int j = 0; j < accs.size(); j++){
                owes.put(accs.get(j), owes.get(accs.get(j)) + (totalThisPayment/accs.size()) - payments.get(i).getAmountPayed(accs.get(j)));
            }
        }
        for(Account a : accounts){
            a.setTotalOwed(Math.round(owes.get(a)*100.)/100.);
            a.setTotalPayed(Math.round(payed.get(a)*100.)/100.);
        }
    }

    @FXML
    private void uninvolveAccountButtonClicked(){
        if(selectedInvolvedAccount != null) {
            HBox tgt = new HBox();
            for(int i = 0; i < involvedAccounts.keySet().size(); i++){
                if(involvedAccounts.get(involvedAccounts.keySet().toArray()[i]).equals(selectedInvolvedAccount)){
                    tgt = (HBox) involvedAccounts.keySet().toArray()[i];
                    involvedAccounts.remove(tgt);
                }
            }
            int selectNext = involvedAccountsListView.getItems().indexOf(tgt);
            if(selectNext == involvedAccountsListView.getItems().size() - 1){
                selectNext = involvedAccountsListView.getItems().size() - 2;
            }
            involvedAccountsListView.getItems().remove(tgt);
            involvedAccountsListView.getSelectionModel().select(selectNext);

            uninvolvedAccountsListView.getItems().add(selectedInvolvedAccount);
            selectedPayment.getAccounts().remove(selectedInvolvedAccount);
            uninvolvedAccountsListView.getSelectionModel().select(uninvolvedAccountsListView.getItems().size() - 1);

            selectedPayment.setAmountPayed(selectedInvolvedAccount, 0);

            involvedAccountsListViewActionPerformed();
            uninvolvedAccountsListViewActionPerformed();
        }

    }

    @FXML
    private void paymentListViewActionPerformed() {
        selectedPayment = (Payment) paymentsListView.getSelectionModel().getSelectedItem();
        if(selectedPayment != null) {
            selectedPaymentNameField.setText(selectedPayment.getName());
            if(selectedPaymentNameField.getText().length() == PAYMENT_NAME_MAX_LENGTH){
                maxPaymentNameErrorLabel.setVisible(true);
            }else{
                maxPaymentNameErrorLabel.setVisible(false);
            }
            paymentsListView.scrollTo(selectedPayment);

            uninvolvedAccountsListView.getItems().clear();
            involvedAccountsListView.getItems().clear();
            for(int i = 0; i < accounts.size(); i++){
                if(!selectedPayment.getAccounts().contains(accounts.get(i))){
                    uninvolvedAccountsListView.getItems().add(accounts.get(i));
                }else{
                    displayInvolvedAccount(accounts.get(i));
                }
            }
        }else{
            maxPaymentNameErrorLabel.setVisible(false);
            selectedPaymentNameField.clear();
            uninvolvedAccountsListView.getItems().clear();
            involvedAccountsListView.getItems().clear();
        }
    }

    @FXML
    private void selectedPaymentNameFieldKeyTyped(KeyEvent k){
        if(selectedPayment != null) {
            selectedPayment.setName(selectedPaymentNameField.getText());
            paymentsListView.refresh();
            accountTotalsTableView.refresh();
            if(selectedPaymentNameField.getText().length() == PAYMENT_NAME_MAX_LENGTH){
                maxPaymentNameErrorLabel.setVisible(true);
            }else{
                maxPaymentNameErrorLabel.setVisible(false);
            }
        }else{
            selectedPaymentNameField.clear();
        }
    }

    @FXML
    private void selectedAccountNameFieldKeyTyped(){
        if(selectedAccount != null) {
            selectedAccount.setName(selectedAccountNameField.getText());
            accountsListView.refresh();
            accountTotalsTableView.refresh();
            if(selectedAccountNameField.getText().length() == ACCOUNT_NAME_MAX_LENGTH){
                maxAccountNameErrorLabel.setVisible(true);
            }else{
                maxAccountNameErrorLabel.setVisible(false);
            }
            updateErrorPane();
        }else{
            selectedAccountNameField.clear();
        }
    }

    private void updateErrorPane(){
        ArrayList<Payment> involvedPayments = getInvolvedPayments(selectedAccount);
        involvedPaymentsList.getItems().clear();
        involvedPaymentsList.getItems().addAll(involvedPayments);
        errorLabel.setText("Cannot delete selected account:\n\"" + selectedAccount.getName() +"\"\nbecause it is involved in the following payment" + (involvedPayments.size() > 1 ? "s:" : ":"));
    }

    @FXML
    private void accountsListViewActionPerformed(){
        selectedAccount = (Account) accountsListView.getSelectionModel().getSelectedItem();
        if(selectedAccount != null) {
            selectedAccountNameField.setText(selectedAccount.getName());
            if(selectedAccountNameField.getText().length() == ACCOUNT_NAME_MAX_LENGTH){
                maxAccountNameErrorLabel.setVisible(true);
            }else{
                maxAccountNameErrorLabel.setVisible(false);
            }
            accountsListView.scrollTo(selectedAccount);
            if(canDeleteAccount(selectedAccount)){
                deleteAccountButton.setDisable(false);
                errorPane.setVisible(false);
            }else{
                deleteAccountButton.setDisable(true);
                errorPane.setVisible(true);
                updateErrorPane();
            }
        }else{
            maxAccountNameErrorLabel.setVisible(false);
            selectedAccountNameField.clear();
            deleteAccountButton.setDisable(true);
            errorPane.setVisible(false);
        }
    }

    @FXML
    private void newPaymentButtonMouseClicked(){
        Payment p = new Payment();
        p.setName("New Payment #" + paymentCount++);
        payments.add(p);
        paymentsListView.getItems().add(p);
        paymentsListView.getSelectionModel().select(payments.size() - 1);
        paymentListViewActionPerformed();
    }

    @FXML
    private void newAccountButtonMouseClicked(){
        Account n = new Account();
        n.setName("New Account #" + accountCount++);
        accounts.add(n);
        accountsListView.getItems().add(n);
        accountTotalsTableView.getItems().add(n);
        // Prevents the horizontal scroll bar from appearing
        if(accounts.size() >= 16){
            owedTableColumn.setPrefWidth(62);
        }else{
            owedTableColumn.setPrefWidth(75);
        }
        accountsListView.getSelectionModel().select(accounts.size() - 1);
        accountsListViewActionPerformed();
    }

    private boolean canDeleteAccount(Account act){
        for(int i = 0; i < payments.size(); i++){
            if(payments.get(i).getAccounts().contains(act)){
                return false;
            }
        }
        return true;
    }

    private ArrayList<Payment> getInvolvedPayments(Account act){
        ArrayList<Payment> out = new ArrayList<>();
        for(int i = 0; i < payments.size(); i++){
            if(payments.get(i).getAccounts().contains(act)){
                out.add(payments.get(i));
            }
        }
        return out;
    }

    @FXML
    private void deleteAccountButtonMouseClicked(){
        if(selectedAccount != null) {
            int selectNext = accountsListView.getItems().indexOf(selectedAccount);
            if (selectNext == accounts.size() - 1) {
                selectNext = accounts.size() - 2;
            }
            accounts.remove(selectedAccount);
            accountsListView.getItems().remove(selectedAccount);
            accountTotalsTableView.getItems().remove(selectedAccount);
            accountsListView.getSelectionModel().select(selectNext);
            accountsListViewActionPerformed();
        }
    }

    @FXML
    private void deletePaymentButtonMouseClicked(){
        if(selectedPayment != null) {
            int selectNext = paymentsListView.getItems().indexOf(selectedPayment);
            if(selectNext == payments.size() - 1){
                selectNext = payments.size() - 2;
            }
            payments.remove(selectedPayment);
            paymentsListView.getItems().remove(selectedPayment);
            accountTotalsTableView.getItems().remove(selectedPayment);
            paymentsListView.getSelectionModel().select(selectNext);
            paymentListViewActionPerformed();
        }
    }
}
