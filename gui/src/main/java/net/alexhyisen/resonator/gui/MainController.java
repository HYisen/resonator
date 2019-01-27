/*
 * This file is part of resonator.
 *
 * resonator is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * resonator is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with resonator.  If not, see <https://www.gnu.org/licenses/>.
 */

package net.alexhyisen.resonator.gui;

import javafx.collections.FXCollections;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import net.alexhyisen.resonator.core.HttpService;
import net.alexhyisen.resonator.core.MqttService;
import net.alexhyisen.resonator.utility.Config;
import net.alexhyisen.resonator.utility.Pair;
import net.alexhyisen.resonator.utility.Utility;

import java.net.InetSocketAddress;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalTime;
import java.util.Map;
import java.util.concurrent.*;
import java.util.stream.Collectors;

public class MainController {
    @FXML
    private TextField usernameTextField;
    @FXML
    private TextField passwordTextField;
    @FXML
    private TextField brokerTextField;
    @FXML
    private TextField clientTextField;
    @FXML
    private TextField topicTextField;
    @FXML
    private TextField pathTextField;
    @FXML
    private Spinner<Integer> timeSpinner;
    @FXML
    private ToggleButton autoToggleButton;
    @FXML
    private ToggleButton linkToggleButton;
    @FXML
    private Spinner<Integer> portSpinner;
    @FXML
    private ToggleButton watchToggleButton;
    @FXML
    private TextField rxTextField;
    @FXML
    private TextField txTextField;
    @FXML
    private TextArea patternTextArea;
    @FXML
    private TableView<Pair<String, String>> dataTableView;

    private Config config = new Config();
    private HttpService httpService;
    private MqttService mqttService;
    private ScheduledExecutorService ses = Executors.newScheduledThreadPool(2);
    private ScheduledFuture<?> sf = null;

    private void setInfoText(String msg) {
        rxTextField.setText(msg);
        txTextField.setText(msg);
    }

    @FXML
    public void initialize() {
        setInfoText("N/A");
        pathTextField.setText("config");

        //noinspection unchecked
        dataTableView.getColumns().setAll(
                genColumn("first", event -> event.getRowValue().setFirst(event.getNewValue())),
                genColumn("second", event -> event.getRowValue().setSecond(event.getNewValue()))
        );
        dataTableView.setEditable(true);
        dataTableView.setItems(FXCollections.observableArrayList());

        timeSpinner.setEditable(true);
        timeSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(
                100, 60000, 4000, 100));

        portSpinner.setEditable(true);
        portSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(
                0, 65535, 8080, 1));
    }

    private static TableColumn<Pair<String, String>, String>
    genColumn(String property, EventHandler<TableColumn.CellEditEvent<Pair<String, String>, String>> handler) {
        TableColumn<Pair<String, String>, String> col = new TableColumn<>(property);
        col.setCellValueFactory(new PropertyValueFactory<>(property));
        col.setCellFactory(TextFieldTableCell.forTableColumn());
        col.setOnEditCommit(handler);
        col.setEditable(true);
        col.setPrefWidth(70.0);
        return col;
    }

    @FXML
    protected void handleLoadButtonAction() {
        var result = config.load(Paths.get(pathTextField.getText()));
        if (result) {
            var data = config.getData();
            usernameTextField.setText(data.get("username"));
            passwordTextField.setText(data.get("password"));
            brokerTextField.setText(data.get("broker"));
            clientTextField.setText(data.get("client"));
            topicTextField.setText(data.get("topic"));
            timeSpinner.getValueFactory().setValue(Integer.valueOf(data.get("delay")));
            portSpinner.getValueFactory().setValue(Integer.valueOf(data.get("port")));
            patternTextArea.setText(data.get("pattern"));
            dataTableView.getItems().clear();
            data
                    .entrySet()
                    .stream()
                    .filter(e -> e.getKey().startsWith("key_") || e.getKey().startsWith("value_"))
                    .collect(Collectors.groupingBy(e -> Integer.valueOf(e.getKey().split("_")[1])))
                    .values()
                    .stream()
                    .map(list -> {
                        var pair = new Pair<String, String>();
                        for (var item : list) {
                            if (item.getKey().startsWith("key_")) {
                                pair.setFirst(item.getValue());
                            } else if (item.getKey().startsWith("value_")) {
                                pair.setSecond(item.getValue());
                            } else {
                                throw new RuntimeException("bad json input " + item.getKey() + " = " + item.getValue());
                            }
                        }
                        return pair;
                    })
                    .forEach(v -> dataTableView.getItems().add(v));
        } else {
            setInfoText("failed to load config");
        }
    }

    @FXML
    protected void handleSaveButtonAction() {
        var data = config.getData();
        data.clear();
        data.put("username", usernameTextField.getText());
        data.put("password", passwordTextField.getText());
        data.put("broker", brokerTextField.getText());
        data.put("client", clientTextField.getText());
        data.put("topic", topicTextField.getText());
        data.put("delay", timeSpinner.getValue().toString());
        data.put("port", portSpinner.getValue().toString());
        data.put("pattern", patternTextArea.getText());
        int cnt = 0;
        for (var pair : dataTableView.getItems()) {
            var k = pair.getFirst();
            var v = pair.getSecond();
            data.put("key_" + cnt, k);
            data.put("value_" + cnt, v);
            cnt++;
        }
        var result = config.save(Path.of(pathTextField.getText()));
        if (result) {
            setInfoText("succeeded to save config");
        } else {
            setInfoText("failed to save config");
        }
    }

    @FXML
    protected void handleAppendSourceButtonAction() {
        var pair = new Pair<>("key", "value");
        dataTableView.getItems().add(pair);
    }

    @FXML
    protected void handleDeleteSourceButtonAction() {
        var pair = dataTableView.getSelectionModel().getSelectedItem();
        dataTableView.getItems().remove(pair);
    }

    @FXML
    protected void handleWatchToggleButtonAction() {
        if (watchToggleButton.isSelected()) {
            httpService = new HttpService(this::dealRxData);
            portSpinner.setDisable(true);
            dealFuture(
                    httpService.start(new InetSocketAddress(portSpinner.getValue())), "start watch",
                    Utility.NOOP, () -> {
                        portSpinner.setDisable(false);
                        watchToggleButton.setSelected(false);
                    }
            );
        } else {
            dealFuture(httpService.stop(), "stop watch", () -> portSpinner.setDisable(false),
                    () -> watchToggleButton.setSelected(true));
        }
    }

    @FXML
    protected void handleAutoToggleButtonAction() {
        if (autoToggleButton.isSelected()) {
            int delay = timeSpinner.getValue();
            sf = ses.scheduleWithFixedDelay(this::handleFireButtonAction, delay, delay, TimeUnit.MILLISECONDS);
            timeSpinner.setDisable(true);
        } else {
            sf.cancel(false);
            timeSpinner.setDisable(false);
        }
    }

    private void setLinkInfoDisable(boolean value) {
        brokerTextField.setDisable(value);
        clientTextField.setDisable(value);
        usernameTextField.setDisable(value);
        passwordTextField.setDisable(value);
    }

    @FXML
    protected void handleLinkToggleButtonAction() {
        if (linkToggleButton.isSelected()) {
            mqttService = new MqttService();
            setLinkInfoDisable(true);
            dealFuture(ses.submit(() -> {
                if (!mqttService.connect(
                        brokerTextField.getText(),
                        clientTextField.getText(),
                        usernameTextField.getText(),
                        passwordTextField.getText()
                )) {
                    throw new RuntimeException();
                }
            }), "connect", Utility.NOOP, () -> {
                linkToggleButton.setSelected(false);
                setLinkInfoDisable(false);
            });
        } else {
            dealFuture(ses.submit(() -> {
                if (!mqttService.disconnect()) {
                    linkToggleButton.setSelected(true);
                    setLinkInfoDisable(true);
                    throw new RuntimeException();
                }
            }), "disconnect", () -> setLinkInfoDisable(false), () -> linkToggleButton.setSelected(true));
        }
    }

    @FXML
    private void handleFireButtonAction() {
        System.out.println("fire!");
        var content = Utility.format(patternTextArea.getText(), dataTableView.getItems());
        System.out.println(content);
        if (linkToggleButton.isSelected()) {
            dealFuture(ses.submit(() -> {
                if (!mqttService.publish(
                        topicTextField.getText(),
                        content
                )) {
                    throw new RuntimeException();
                }
            }), "publish", Utility.NOOP, Utility.NOOP);
        } else {
            MqttService.oneShot(
                    topicTextField.getText(),
                    content,
                    brokerTextField.getText(),
                    clientTextField.getText(),
                    usernameTextField.getText(),
                    passwordTextField.getText()
            );
        }
        txTextField.setText(LocalTime.now().toString());
    }

    //Promise in JavaScript. CompletableFuture::handle is ugly, and I don't need a return value there.
    private void dealFuture(Future operation, String taskDesc, Runnable passHandler, Runnable failHandler) {
        System.out.println(taskDesc + " 0");
        try {
            operation.get(10, TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            e.printStackTrace();
            setInfoText("failed to " + taskDesc);
            failHandler.run();
            return;
        }
        passHandler.run();
        System.out.println(taskDesc + " 1");
    }

    private void dealRxData(Map<String, String> data) {
        //God bless us if it's concurrent.
        var items = dataTableView.getItems();
        items.clear();
        data.forEach((k, v) -> items.add(new Pair<>(k, v)));
        rxTextField.setText(LocalTime.now().toString());
    }

    void handleCloseEvent() {
        System.out.println(autoToggleButton.isSelected());
        System.out.println(watchToggleButton.isSelected());
        if (autoToggleButton.isSelected()) {
            autoToggleButton.fire();
        }
        if (linkToggleButton.isSelected()) {
            linkToggleButton.fire();
        }
        if (watchToggleButton.isSelected()) {
            watchToggleButton.fire();
        }
        ses.shutdown();
    }
}
