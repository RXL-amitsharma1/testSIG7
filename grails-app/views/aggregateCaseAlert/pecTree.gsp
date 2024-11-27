<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head>
    <meta name="layout" content="mainWithoutTopNav"/>
    <title>PEC Importance</title>
    <asset:stylesheet src="pecTreeStyle" />
    <asset:javascript src="d3/d3.js" />
    <asset:javascript src="d3/d3.layout.js" />

    <style>
    .node circle {
        cursor: pointer;
        fill: #fff;
        stroke: steelblue;
        stroke-width: 2.5px;
    }
    .node text {
        font-size: 15px;
        display: inline-block;
        width: 50px;
        border: 1px solid #000;
        text-align: center;
    }
    path.link {
        fill: none;
        stroke: #ccc;
        stroke-width: 1.5px;
    }
    </style>
</head>
<body style="overflow-y: scroll;">
<div id="body"></div>
<script type="text/javascript">

    var m = [20, 120, 20, 120],
        w = 1280 - m[1] - m[3],
        h = 800 - m[0] - m[2],
        i = 0,
        root;

    var tree = d3.layout.tree().size([h, w]);

    var diagonal = d3.svg.diagonal().projection(function(d) { return [d.y, d.x]; });

    var vis = d3.select("#body").append("svg:svg")
        .attr("width", 1380)
        .attr("style", "padding-left:150px")
        .attr("height", 900)
        .append("svg:g")
        .attr("transform", "translate(" + m[3] + "," + m[0] + ")");

    d3.json("pecTreeJson", function(json) {
        root = json;
        root.x0 = h / 2;
        root.y0 = 0;
        function toggleAll(d) {
            if (typeof d != "undefined") {
                if (d.children) {
                    d.children.forEach(toggleAll);
                    toggle(d);
                }
            }
        }
        // Initialize the display to show a few nodes.
        root.children.forEach(toggleAll);
        toggle(root.children[0]);
        //toggle(root.children[0].children[0]);
        toggle(root.children[0].children[1]);
        //toggle(root.children[0].children[1].children[0]);
        toggle(root.children[0].children[1].children[1]);
        toggle(root.children[0].children[1].children[1].children[1]);
        //toggle(root.children[0].children[2]);
        //toggle(root.children[0].children[2].children[0]);
        //toggle(root.children[0].children[2].children[1]);
        //toggle(root.children[0].children[3]);
        //toggle(root.children[0].children[3].children[1]);
        //toggle(root.children[1]);
        //toggle(root.children[2]);
        update(root);
    });

    function update(source) {
        var duration = d3.event && d3.event.altKey ? 5000 : 500;

        // Compute the new tree layout.
        var nodes = tree.nodes(root).reverse();

        // Normalize for fixed-depth.
        nodes.forEach(function(d) { d.y = d.depth * 180; });

        // Update the nodes…
        var node = vis.selectAll("g.node")
            .data(nodes, function(d) { return d.id || (d.id = ++i); });

        // Enter any new nodes at the parent's previous position.
        var nodeEnter = node.enter().append("svg:g")
            .attr("class", "node")
            .attr("transform", function(d) { return "translate(" + source.y0 + "," + source.x0 + ")"; })
            .on("click", function(d) {
                var nameObj = d.name
                if (nameObj == "Strength(62.5, 37.5)") {
                    var titleObj = d.title
                    var matrix = d.matrix
                    var matrixArr = matrix.split(",")
                    var th1 = matrixArr[0].split(":")[0];
                    var th2 = matrixArr[1].split(":")[0];
                    var tbody1 = matrixArr[0].split(":")[1];
                    var tbody2 = matrixArr[1].split(":")[1];

                    var table = "<table class='table' >"
                    table = table + "<thead><th>" +  th1 + "</th><th>"+  th2 + "</th></thead>"
                    table = table + "<tbody><tr><td>" +  tbody1 + "</td><td>" +  tbody2 + "</td><tr></tbody>"
                    table = table + "</table>"

                    var caseDiffModal = $('#bayesianNetworkModal');
                    caseDiffModal.find('.name').html(titleObj);
                    caseDiffModal.find(".modal-body").find('.matrix').html(table);
                    caseDiffModal.modal('show');
                }
                toggle(d);
                update(d);
            });

        nodeEnter.append("svg:circle")
            .attr("r", 1e-6)
            .style("fill", function(d) { return d._children ? "lightsteelblue" : "#fff"; });

        nodeEnter.append("svg:text")
            .attr("x", function(d) { return d.children || d._children ? -10 : 10; })
            .attr("dy", ".35em")
            .attr("text-anchor", function(d) { return d.children || d._children ? "end" : "start"; })
            .text(function(d) { return d.name; })
            .style("fill-opacity", 1e-6);

        // Transition nodes to their new position.
        var nodeUpdate = node.transition()
            .duration(duration)
            .attr("transform", function(d) { return "translate(" + d.y + "," + d.x + ")"; });

        nodeUpdate.select("circle")
            .attr("r", 4.5)
            .style("fill", function(d) { return d._children ? "lightsteelblue" : "#fff"; });

        nodeUpdate.select("text")
            .style("fill-opacity", 1);

        // Transition exiting nodes to the parent's new position.
        var nodeExit = node.exit().transition()
            .duration(duration)
            .attr("transform", function(d) { return "translate(" + source.y + "," + source.x + ")"; })
            .remove();

        nodeExit.select("circle")
            .attr("r", 1e-6);

        nodeExit.select("text")
            .style("fill-opacity", 1e-6);

        // Update the links…
        var link = vis.selectAll("path.link")
            .data(tree.links(nodes), function(d) { return d.target.id; });

        // Enter any new links at the parent's previous position.
        link.enter().insert("svg:path", "g")
            .attr("class", "link")
            .attr("d", function(d) {
                var o = {x: source.x0, y: source.y0};
                return diagonal({source: o, target: o});
            })
            .transition()
            .duration(duration)
            .attr("d", diagonal);

        // Transition links to their new position.
        link.transition()
            .duration(duration)
            .attr("d", diagonal);

        // Transition exiting nodes to the parent's new position.
        link.exit().transition()
            .duration(duration)
            .attr("d", function(d) {
                var o = {x: source.x, y: source.y};
                return diagonal({source: o, target: o});
            })
            .remove();

        // Stash the old positions for transition.
        nodes.forEach(function(d) {
            d.x0 = d.x;
            d.y0 = d.y;
        });
    }

    // Toggle children.
    function toggle(d) {
        if (typeof d != "undefined") {
            if (d.children) {
                d._children = d.children;
                d.children = null;
            } else {
                d.children = d._children;
                d._children = null;
            }
        }
    }


</script>
<br/>
<br/>
<g:render template="/includes/modals/bayesian_network_modal" />
</body>
</html>