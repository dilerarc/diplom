function chart() {
    var data = [ ];

    data.push([ 1, 2 ]);

    var plot = $.plot("#placeholder", [
        {
            data: data, label: "Labbel"
        }
    ], {
        legend: {
            backgroundOpacity: 0.8,
            position: "sw"
        },
        series: {
            lines: {
                show: true
            },
            points: {
                show: true
            }
        },
        grid: {
            hoverable: true
        },
        xaxis: {
            mode: "time",
            minTickSize: [ 1, "day" ]
        },
        yaxes: [
            { position: "left", min: 0 },
            { position: "right", min: 0 },
            { position: "right", min: 0 }
        ]
    });

    $("<div id='tooltip'></div>").css({
        position: "absolute",
        display: "none",
        border: "1px solid #fdd",
        padding: "2px",
        "background-color": "#fee",
        opacity: 0.80
    }).appendTo("body");

    $("#placeholder").bind("plothover", function (event, pos, item) {

        if (item) {
            var x = item.datapoint[ 0 ],
                y = item.datapoint[ 1 ];

            $("#tooltip").html(item.series.label + ". " + moment(new Date(x)).format("YYYY-MM-DD") + " : " + y)
                .css({ top: item.pageY + 5, left: item.pageX + 5 })
                .fadeIn(200);
        } else {
            $("#tooltip").hide();
        }

    });
}