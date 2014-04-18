function charts(itemId, label, units, period) {
    var placeholderId = "placeholder" + itemId
    $("#charts").append("<div id='{0}' class='placeholderAll'></div>".format(placeholderId))
    chart(itemId, label, units, period, placeholderId)
}

function chart(itemId, label, units, period, placeholder) {

    var plot = $.plot("#" + placeholder, [
        {
            data: [],
            label: label + ", " + units
        }
    ], {
        series: {
            lines: { show: true, fill: true }
        },
        xaxis: {
            timeformat: "%H:%M:%S",
            mode: "time",
            minTickSize: [ 1, "second" ]
        }
    });

    function update() {
        var d = getData(itemId, period);
        if(d.length > 0){
            var newData = JSON.parse(d).map(function (obj) {
                return [obj.date, obj.data];
            });

            plot.setData([
                {
                    data: newData, label: label + ", " + units
                }
            ]);
            plot.setupGrid();
            plot.draw();

        }
        setTimeout(update, 2000);

    }

    update()
}

function getData(itemId, period) {

    var result = "";

    var request = $.ajax({
        async: false,
        url: "/charts/{0}/data?period={1}".format(itemId, period)
    });

    request.done(function (data) {
        if(data == "ERROR") return result;
        result = data;
    });

    request.fail(function (jqXHR, textStatus) {
        console.log("Request failed: " + textStatus)
    });

    return result;
}

String.prototype.format = function () {
    var args = arguments;

    return this.replace(/\{(\w+)\}/g, function () {
        return args[arguments[1]];
    });
};
