function chart(itemId, label, units, data) {

    var mainData = data.map(function (obj) {
        return [obj.date, obj.data];
    });

    var lastDate = mainData[mainData.length - 1][0]

    var plot = $.plot("#placeholder", [
        {
            data: mainData, label: label
        }
    ], {
        xaxis: {
            timeformat: "%y-%m-%d %H:%M:%S",
            mode: "time",
            minTickSize: [ 1, "second" ]/*,
             min: (new Date(2014, 3, 1, 14, 20, 0, 0)).getTime(),
             max: (new Date(2014, 3, 1, 14, 21, 0, 0)).getTime()*/
        }
    });

    function update() {

        var newData = JSON.parse(getData(itemId, lastDate));

        newData = newData.map(function (obj) {
            return [obj.date, obj.data];
        });

        if (newData.length > 0) {
            mainData = mainData.concat(newData);
            lastDate = newData[newData.length - 1][0];
        }

        plot.setData([
            {
                data: mainData
            }
        ]);
        plot.setupGrid();
        plot.draw();

        setTimeout(update, 2000);
    }

    update()
}

function getData(itemId, date) {

    var result = [];

    var request = $.ajax({
        async: false,
        url: "/charts/{0}/delta?time={1}".format(itemId, date)
    });

    request.done(function (data) {
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
