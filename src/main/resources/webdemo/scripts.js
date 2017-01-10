google.charts.load('current', {'packages': ['corechart', 'gauge', 'bar']});

var gaugeOptions = {
    redFrom: 0,
    redTo: 40,
    yellowFrom: 40,
    yellowTo: 80,
    greenFrom: 80,
    greenTo: 100,
    minorTicks: 5,
    width: 130,
    height: 130
};
var gaugeLevelsOptions = {
    redFrom: 0,
    redTo: 40,
    yellowFrom: 40,
    yellowTo: 80,
    greenFrom: 80,
    greenTo: 100,
    minorTicks: 5,
    width: 360,
    height: 120
};
var posChartOptions = {
    width: 500,
    height: 300,
    orientation: "vertical",
    legend: {
        position: 'none'
    }
};

function escapeAttrNodeValue(value) {
    return value.replace(/(&)|(")|(\u00A0)/g, function (match, amp, quote) {
        if (amp) return "&amp;";
        if (quote) return "&quot;";
        return "&nbsp;";
    });
}

function addLI(ul, title, value) {
    var li = $("<li></li>");
    li.addClass("list-group-item");
    var span = $("<span></span>");
    span.addClass("badge");
    span.append(value);
    li.append(span);
    li.append(" " + title);
    // var b = $("<b></b>");
    // b.append(title);
    // li.append(b);
    // li.append(" " + value);
    ul.append(li);
}

$(function () {
    $('#select_examples').change(function () {
        $('#text').val($('#select_examples').val());
    });

    $('button.has-spinner').click(function () {

        linguisticAnnotations = $('#linguistic-annotations-checkbox').prop("checked");
        dashboard = $('#dashboard-checkbox').prop("checked");

        if (!linguisticAnnotations && !dashboard) {
            alert("You must select at least one checkbox");
            return false;
        }

        var text = $('#text').val();
        if (text.length < 10) {
            alert("You must enter at least 10 chars of text");
            return false;
        }

        $(this).toggleClass('active');
        $(this).toggleClass('disabled');
        $('#text').attr('disabled', 'disabled');

        $.ajax("simp", {
            dataType: "json",
            data: {
                text: text
            },
            success: function (data) {

                $("#part1").slideUp(500);

                var language = data.readability.language;
                $(".show-" + language).show();
                // Show language

                if (dashboard) {

                    var tooLongSentences = data.readability.tooLongSentences;
                    var textLen = text.length;

                    $.each(data.sentences, function (i, item) {
                        var p = $("<p></p>");

                        var text = item.text;
                        var beginSentence = item.characterOffsetBegin;
                        var endSentence = item.characterOffsetEnd;

                        var offsets = new Array();
                        for (var i = 0; i < text.length; i++) {
                            offsets[i] = i;
                        }

                        data.languagetool.forEach(function (item) {
                            var originalOffset = item.offset;

                            var originalLength = item.length;
                            if (originalOffset < beginSentence || originalOffset >= endSentence) {
                                return;
                            }

                            originalOffset -= beginSentence;
                            var offset = offsets[originalOffset];
                            var offEnd = offsets[originalOffset + originalLength];
                            var formID = "form_" + beginSentence + "_" + originalOffset;

                            var before = '<a data-content="' + item.message.replace('"', "'") +
                                '" title="' + item.shortMessage.replace('"', "'") +
                                '" tabindex="0" role="button" class="my-popover label label-danger" id="' +
                                formID + '">';
                            var after = '</a>';
                            var newText = text.substring(0, offset);
                            newText += before;
                            newText += text.substring(offset, offEnd);
                            newText += after;
                            newText += text.substring(offEnd);

                            for (var i = originalOffset; i < text.length; i++) {
                                offsets[i] += before.length;
                            }
                            for (var i = originalOffset + originalLength; i < text.length; i++) {
                                offsets[i] += after.length;
                            }

                            text = newText;
                        });

                        p.append(text);
                        p.attr("id", "sentence" + i);
                        p.addClass("sentence");
                        if ($.inArray(item.index, tooLongSentences) > -1) {
                            p.addClass("too-long")
                        }
                        $("#parsed-text").append(p);
                    });

                    $("#part2").tooltip({
                        selector: '.too-long',
                        title: "Sentence too long"
                    });
                    $("#part2").popover({
                        selector: ".my-popover",
                        trigger: "focus"
                    });

                    // Gauges

                    var mainValue = 0;
                    var mainName = "";
                    if (language == "it") {
                        mainValue = data.readability.measures.gulpease;
                        mainName = "Gulpease";
                    }
                    if (language == "en") {
                        mainValue = data.readability.measures.flesch;
                        mainName = "Flesch";
                    }
                    if (language == "es") {
                        mainValue = data.readability.measures['flesch-szigriszt'];
                        mainName = "Flesch-Szigriszt";
                    }
                    var level1 = (isNaN(data.readability.measures.level1) ? 0 : data.readability.measures.level1);
                    var level2 = (isNaN(data.readability.measures.level2) ? 0 : data.readability.measures.level2);
                    var level3 = (isNaN(data.readability.measures.level3) ? 0 : data.readability.measures.level3);

                    var gulpeaseChart = new google.visualization.Gauge(document.getElementById('gauge-gulpease'));
                    gulpeaseChart.draw(google.visualization.arrayToDataTable([
                        ['Label', 'Value'],
                        [mainName, mainValue]
                    ]), gaugeOptions);

                    var levelsChart = new google.visualization.Gauge(document.getElementById('gauge-levels'));
                    levelsChart.draw(google.visualization.arrayToDataTable([
                        ['Label', 'Value'],
                        ['Level1', level1],
                        ['Level2', level2],
                        ['Level3', level3]
                    ]), gaugeLevelsOptions);

                    // Statistics

                    var ul = $('<ul></ul>');
                    ul.addClass("list-group");
                    addLI(ul, "Language:", data.readability.language);
                    addLI(ul, "Sentences:", data.readability.sentenceCount);
                    addLI(ul, "Tokens:", data.readability.tokenCount);
                    addLI(ul, "Words:", data.readability.wordCount);
                    addLI(ul, "Content words:", data.readability.contentWordSize);
                    $("#statistics").append(ul);

                    // Pos chart

                    var posData = new google.visualization.DataTable();
                    posData.addColumn("string", "POS tag");
                    posData.addColumn("number", "Count");
                    $.each(data.readability.genericPosDescription, function (key, value) {
                        var num = data.readability.genericPosStats.support[key];
                        posData.addRow([value, num]);
                    });
                    var posChart = new google.visualization.ColumnChart(document.getElementById('pos-stats'));
                    posChart.draw(posData, posChartOptions);

                    if (textLen < 1000) {
                        var posDiv = $("#pos-distribution");
                        posDiv.appendTo($("#l-col"));
                    }

                    $("#part2").show();
                }

                // Stanford stuff

                if (linguisticAnnotations) {

                    if (typeof data == undefined || data.sentences == undefined) {
                        alert("Failed to reach server!");
                    } else {
                        // Empty divs
                        $('#annotations').empty();
                        // Re-render divs
                        function createAnnotationDiv(id, annotator, selector, label) {
                            // (make sure we requested that element)
                            if (annotators().indexOf(annotator) < 0) {
                                return;
                            }
                            // (make sure the data contains that element)
                            ok = false
                            if (typeof data[selector] != 'undefined') {
                                ok = true;
                            } else if (typeof data.sentences != 'undefined' && data.sentences.length > 0) {
                                if (typeof data.sentences[0][selector] != 'undefined') {
                                    ok = true;
                                } else if (typeof data.sentences[0].tokens != 'undefined' && data.sentences[0].tokens.length > 0) {
                                    ok = (typeof data.sentences[0].tokens[0][selector] != 'undefined');
                                }
                            }

                            // (render the element)
                            if (ok) {
                                $('#annotations').append('<h4 class="red">' + label + ':</h4> <div id="' + id + '"></div>');
                            }
                        }

                        // (create the divs)
                        //                  div id      annotator     field_in_data                          label
                        createAnnotationDiv('pos', 'pos', 'pos', 'Part-of-Speech');
                        // createAnnotationDiv('lemma',    'lemma',      'lemma',                               'Lemmas'                  );
                        // createAnnotationDiv('ner', 'ner', 'ner', 'Named Entity Recognition');
                        // createAnnotationDiv('deps', 'depparse', 'basic-dependencies', 'Basic Dependencies');
                        // createAnnotationDiv('deps2',    'depparse',   'enhanced-plus-plus-dependencies',     'Enhanced++ Dependencies' );
                        // createAnnotationDiv('openie',   'openie',     'openie',                              'Open IE'                 );
                        // createAnnotationDiv('coref',    'coref',      'corefs',                              'Coreference'             );
                        // createAnnotationDiv('entities', 'entitylink', 'entitylink',                          'Wikidict Entities'       );
                        // createAnnotationDiv('kbp',      'kbp',        'kbp',                                 'KBP Relations'           );
                        // createAnnotationDiv('sentiment','sentiment',  'sentiment',                           'Sentiment'               );

                        // Render
                        render(data);
                        $("#part3").show();
                    }
                }

            }
        })
        ;
        return false;
    })
    ;
})
;
