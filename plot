<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>Curves</title>
<script src="https://cdn.plot.ly/plotly-1.52.2.min.js"></script>
</head>
<body>
<div id="chart"></div>
<script>
(function () {
  var data0 = {"x":[0.0,0.1,0.2,0.30000000000000004,0.4,0.5,0.6000000000000001,0.7000000000000001,0.8,0.9,1.0,1.1,1.2000000000000002,1.3,1.4000000000000001,1.5,1.6,1.7000000000000002,1.8,1.9000000000000001,2.0,2.1,2.2,2.3000000000000003,2.4000000000000004,2.5,2.6,2.7,2.8000000000000003,2.9000000000000004,3.0,3.1,3.2,3.3000000000000003,3.4000000000000004,3.5,3.6,3.7,3.8000000000000003,3.9000000000000004,4.0,4.1000000000000005,4.2,4.3,4.4,4.5,4.6000000000000005,4.7,4.800000000000001,4.9,5.0,5.1000000000000005,5.2,5.300000000000001,5.4,5.5,5.6000000000000005,5.7,5.800000000000001,5.9,6.0,6.1000000000000005,6.2,6.300000000000001,6.4,6.5,6.6000000000000005,6.7,6.800000000000001,6.9,7.0,7.1000000000000005,7.2,7.300000000000001,7.4,7.5,7.6000000000000005,7.7,7.800000000000001,7.9,8.0,8.1,8.200000000000001,8.3,8.4,8.5,8.6,8.700000000000001,8.8,8.9,9.0,9.1,9.200000000000001,9.3,9.4,9.5,9.600000000000001,9.700000000000001,9.8,9.9,10.0],"name":"Approx twice","type":"scatter","y":[0.06502552461694197,-0.34420514072492586,0.343600240613025,0.842587786877349,-0.06574078704486408,-1.0114976655894772,-0.2568569938550347,1.4143751702641962,1.3359315855436702,1.181239498428574,3.1685846463171607,1.8735152280077934,1.675755635354846,2.0087119566849077,1.9933486291053537,4.047090021141626,3.2546492375061598,2.3895467223130726,3.5197764263485354,4.35066787344483,3.4435522506447223,4.351824757806369,5.949318611636218,3.5271396517173264,3.4258722248617794,3.9533106208892423,6.060833744609401,5.353561700075752,6.26128610448821,4.9744745230677285,5.89304416609221,5.910178771512472,6.650173515709616,5.536656893175416,6.3661385592144955,8.200274217568008,5.963218359476423,6.929550338778975,6.265390433379342,8.38384888037395,8.818863505732077,8.771088769442688,7.8714115286192445,9.684596011940013,10.664034298562376,8.208524596098982,8.297620122592253,10.331732460780952,9.832036461098888,8.170081851256079,11.281180976652205,9.814901249494955,11.388310810969555,9.657941269019728,10.537309251170463,10.363583921583839,12.473973975566091,9.937552237618288,11.897390003866175,10.94425032271413,12.539395860272363,11.76812103220229,13.25001634196801,13.116609217490762,14.51044862275603,12.425618571834754,11.497621038587086,13.579346759393982,12.57268156324756,14.885708215444595,14.65582053991939,12.239369229969347,12.813580846079086,14.966106242470538,15.698404736473742,15.927405050305662,13.757073544843141,15.670480531516956,15.729207639588958,15.304153884429661,16.94660523724212,16.82061738664951,15.611402649371422,16.113355619468496,18.781605528485745,16.292507547180623,15.443042147703252,16.224338814462055,18.851887174877227,17.637467748446664,19.15707255719781,18.8301716785569,16.775965100629744,18.90238716605365,19.095703980644515,18.258524920372217,20.290074733400694,19.27909749850994,21.34174101144303,19.093941000475294,20.007328207943427]};
  var data1 = {"x":[0.0,0.1,0.2,0.30000000000000004,0.4,0.5,0.6000000000000001,0.7000000000000001,0.8,0.9,1.0,1.1,1.2000000000000002,1.3,1.4000000000000001,1.5,1.6,1.7000000000000002,1.8,1.9000000000000001,2.0,2.1,2.2,2.3000000000000003,2.4000000000000004,2.5,2.6,2.7,2.8000000000000003,2.9000000000000004,3.0,3.1,3.2,3.3000000000000003,3.4000000000000004,3.5,3.6,3.7,3.8000000000000003,3.9000000000000004,4.0,4.1000000000000005,4.2,4.3,4.4,4.5,4.6000000000000005,4.7,4.800000000000001,4.9,5.0,5.1000000000000005,5.2,5.300000000000001,5.4,5.5,5.6000000000000005,5.7,5.800000000000001,5.9,6.0,6.1000000000000005,6.2,6.300000000000001,6.4,6.5,6.6000000000000005,6.7,6.800000000000001,6.9,7.0,7.1000000000000005,7.2,7.300000000000001,7.4,7.5,7.6000000000000005,7.7,7.800000000000001,7.9,8.0,8.1,8.200000000000001,8.3,8.4,8.5,8.6,8.700000000000001,8.8,8.9,9.0,9.1,9.200000000000001,9.3,9.4,9.5,9.600000000000001,9.700000000000001,9.8,9.9,10.0],"name":"Exp","type":"scatter","y":[1.0,1.1051709180756477,1.2214027581601699,1.3498588075760032,1.4918246976412703,1.6487212707001282,1.822118800390509,2.0137527074704766,2.225540928492468,2.45960311115695,2.718281828459045,3.0041660239464334,3.320116922736548,3.6692966676192444,4.055199966844675,4.4816890703380645,4.953032424395115,5.473947391727201,6.0496474644129465,6.68589444227927,7.38905609893065,8.166169912567652,9.025013499434122,9.974182454814724,11.023176380641605,12.182493960703473,13.463738035001692,14.879731724872837,16.444646771097055,18.174145369443067,20.085536923187668,22.197951281441636,24.532530197109352,27.112638920657893,29.964100047397025,33.11545195869231,36.59823444367799,40.4473043600674,44.701184493300836,49.40244910553019,54.598150033144236,60.340287597362,66.68633104092515,73.69979369959579,81.45086866496814,90.01713130052181,99.48431564193386,109.94717245212352,121.51041751873497,134.28977968493552,148.4131591025766,164.02190729990184,181.27224187515122,200.33680997479183,221.40641620418717,244.69193226422038,270.42640742615276,298.8674009670603,330.2995599096489,365.0374678653289,403.4287934927351,445.85777008251716,492.7490410932563,544.5719101259294,601.8450378720822,665.1416330443618,735.0951892419732,812.4058251675433,897.8472916504184,992.2747156050262,1096.6331584284585,1211.9670744925775,1339.430764394418,1480.2999275845464,1635.984429995927,1808.0424144560632,1998.195895104119,2208.347991887209,2440.6019776245007,2697.28232826851,2980.9579870417283,3294.4680752838403,3640.9503073323585,4023.872393822313,4447.066747699858,4914.768840299134,5431.659591362978,6002.912217261029,6634.24400627789,7331.973539155995,8103.083927575384,8955.292703482508,9897.129058743927,10938.019208165191,12088.380730216988,13359.726829661873,14764.781565577294,16317.60719801545,18033.744927828524,19930.370438230297,22026.465794806718]};

  var data = [data0, data1];
  var layout = {"title":"Curves"};
 var config = {};

  Plotly.plot('chart', data, layout, config);
})();
</script>
</body>
</html>
       