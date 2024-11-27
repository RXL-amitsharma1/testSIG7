$(function(){
    //modify buttons style
    $.fn.editableform.buttons =
    '<button type="submit" class="btn btn-primary editable-submit btn-sm waves-effect waves-light"><i class="md md-done"></i></button>' +
    '<button type="button" class="btn editable-cancel btn-sm waves-effect waves-light"><i class="md md-clear"></i></button>';

    //editables
    $('.editable-txt').editable({
     type: $(this).attr("type"),
     pk: 1,
     name: $(this).attr("name")
   });

    $('#firstname').editable({
      validate: function(value) {
       if($.trim(value) == '') return 'This field is required';
     }
   });

   $('.edit-Country').editable({
     prepend: "USA",
     source: [
     {value: 1, text: 'USA'},
     {value: 2, text: 'India'},
     {value: 3, text: 'Japan'},
     {value: 4, text: 'China'}
     ],
     display: function(value, sourceData) {
      //var colors = {"": "gray", 1: "green", 2: "blue"},
      elem = $.grep(sourceData, function(o){return o.value == value;});

      if(elem.length) {
        $(this).text(elem[0].text);
      } else {
        $(this).empty();
      }
    }
  });
  $('.editable-yes').editable({
    prepend: "Yes",
    source: [
    {value: 1, text: 'Yes'},
    {value: 2, text: 'No'}
    ],
    display: function(value, sourceData) {
     //var colors = {"": "gray", 1: "green", 2: "blue"},
     elem = $.grep(sourceData, function(o){return o.value == value;});

     if(elem.length) {
       $(this).text(elem[0].text);
     } else {
       $(this).empty();
     }
   }
  });

    $('#sex').editable({
      prepend: "not selected",
      source: [
      {value: 1, text: 'Male'},
      {value: 2, text: 'Female'}
      ],
      display: function(value, sourceData) {
       var colors = {"": "gray", 1: "green", 2: "blue"},
       elem = $.grep(sourceData, function(o){return o.value == value;});

       if(elem.length) {
         $(this).text(elem[0].text).css("color", colors[value]);
       } else {
         $(this).empty();
       }
     }
   });

    $('#status').editable();

    $('#group').editable({
      showbuttons: false
    });

    $('#dob').editable();

    $('#comments').editable({
      showbuttons: 'bottom'
    });

  });
