/**
 * Grid theme for Highcharts JS
 * @author Torstein H√∏nsi
 */

Highcharts.theme = {
	colors: ['#058DC7', '#50B432', '#ED561B', '#DDDF00', '#24CBE5', '#64E572', '#FF9655', '#FFF263', '#6AF9C4'],
	chart: {
		backgroundColor: {
			linearGradient: [0, 0, 0, 500],
			stops: [
				[0, 'rgb(250, 250, 250)'],
				[1, 'rgb(220, 220, 240)']
			]
		},
		borderWidth: 0,
		plotBackgroundColor: 'rgba(255, 255, 255, .9)',
		plotShadow: false,
		plotBorderWidth: 0
	},
	title: {
		style: { 
			color: '#000',
			font: 'bold 16px "Trebuchet MS", Verdana, sans-serif'
		}
	},
	subtitle: {
		style: { 
			color: '#666666',
			font: 'bold 12px "Trebuchet MS", Verdana, sans-serif'
		}
	},
	xAxis: {
		gridLineWidth: 0,
		lineColor: 'lightgrey',
		tickColor: 'ilghtgrey',
		labels: {
			style: {
				color: 'grey',
				font: '11px Trebuchet MS, Verdana, sans-serif'
			}
		},
		title: {
			style: {
				color: 'grey',
				fontWeight: 'bold',
				fontSize: '12px',
				fontFamily: 'Trebuchet MS, Verdana, sans-serif'

			}				
		}
	},
	yAxis: {
		minorTickInterval: 0,
		majorTickInterval: 100,
		lineColor: 'lightgrey',
		lineWidth: 1,
		tickWidth: 1,
		tickColor: 'lightgrey',
		labels: {
			style: {
				color: 'darkgrey',
				font: '11px Trebuchet MS, Verdana, sans-serif'
			}
		},
		title: {
			style: {
				color: '#333',
				fontWeight: 'bold',
				fontSize: '12px',
				fontFamily: 'Trebuchet MS, Verdana, sans-serif'
			}				
		}
	},
	legend: {
		itemStyle: {			
			font: '9pt Trebuchet MS, Verdana, sans-serif',
			color: 'grey'

		},
		itemHoverStyle: {
			color: '#039'
		},
		itemHiddenStyle: {
			color: 'gray'
		},
	},
	labels: {
		style: {
			color: '#99b'
		}
	},
};

// Apply the theme
var highchartsOptions = Highcharts.setOptions(Highcharts.theme);

